package com.example.budgetbudgie

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetbudgie.data.SharedBudget
import com.example.budgetbudgie.data.SharedExpense
import com.example.budgetbudgie.data.SharedMember
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*
import com.google.firebase.database.Query

class SharedBudgetActivity : AppCompatActivity() {

    // Firebase refs
    private val budgetsRef = FirebaseDatabase.getInstance().getReference("sharedBudgets")
    private val expensesRef = FirebaseDatabase.getInstance().getReference("sharedExpenses")
    private val membersRef = FirebaseDatabase.getInstance().getReference("sharedMembers")

    // UI
    private lateinit var container: LinearLayout
    private lateinit var btnAddBudget: Button

    // listeners per budget
    private val budgetListeners =
        mutableMapOf<String, Pair<ValueEventListener, ValueEventListener>>()

    private val expenseQueryRefs = mutableMapOf<String, Query>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shared_budget)

        container = findViewById(R.id.budgetContainer)
        btnAddBudget = findViewById(R.id.btnAddBudget)

        btnAddBudget.setOnClickListener { showAddBudgetDialog() }

        loadBudgets()
        setupBottomNav()
    }

    // Load budgets
    private fun loadBudgets() {
        budgetsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                detachAllChildListeners()
                container.removeAllViews()

                for (child in snapshot.children) {
                    val budget = child.getValue(SharedBudget::class.java) ?: continue
                    inflateAndBindBudgetCard(budget)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                toast("Failed to load budgets: ${error.message}")
            }
        })
    }

    // Create budget card and bind data
    private fun inflateAndBindBudgetCard(budget: SharedBudget) {

        val card = LayoutInflater.from(this)
            .inflate(R.layout.item_budget, container, false)

        val tvTitle = card.findViewById<TextView>(R.id.tvBudgetName)
        val tvAmount = card.findViewById<TextView>(R.id.tvBudgetAmount)
        val tvSpent = card.findViewById<TextView>(R.id.tvSpent)
        val tvRemaining = card.findViewById<TextView>(R.id.tvRemaining)
        val progressBudget = card.findViewById<ProgressBar>(R.id.progressBudget)
        val membersContainer = card.findViewById<LinearLayout>(R.id.membersContainer)
        val tvMemberCount = card.findViewById<TextView>(R.id.tvMemberCount)
        val btnAddMember = card.findViewById<Button>(R.id.btnAddMember)
        val btnAddExpense = card.findViewById<Button>(R.id.btnAddExpense)
        val expenseContainer = card.findViewById<LinearLayout>(R.id.expenseContainer)

        tvTitle.text = budget.name
        tvAmount.text = "Total Budget: R${budget.totalBudget}"

        // default owner row
        membersContainer.removeAllViews()
        membersContainer.addView(createMemberRow("You", "Owner", null, null))
        tvMemberCount.text = "1 member"

        // member updates
        val memberListener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                membersContainer.removeAllViews()
                membersContainer.addView(createMemberRow("You", "Owner", null, null))

                val members = snap.children.mapNotNull {
                    it.getValue(SharedMember::class.java)
                }

                for (m in members) {
                    membersContainer.addView(
                        createMemberRow(m.name, m.role, budget.id, m.id)
                    )
                }

                val total = members.size + 1
                tvMemberCount.text = "$total members"
            }

            override fun onCancelled(error: DatabaseError) {
                toast("Failed to load members")
            }
        }

        membersRef.child(budget.id).addValueEventListener(memberListener)

        // expense updates
        val expenseQuery = expensesRef.orderByChild("budgetId").equalTo(budget.id)

        val expenseListener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                expenseContainer.removeAllViews()
                var totalSpent = 0.0

                for (e in snap.children) {
                    val exp = e.getValue(SharedExpense::class.java) ?: continue
                    totalSpent += exp.amount
                    expenseContainer.addView(createExpenseRow(exp))
                }

                val remaining = budget.totalBudget - totalSpent

                tvSpent.text = "R%.2f".format(totalSpent)
                tvRemaining.text = "R%.2f".format(remaining)

                val progress = if (budget.totalBudget > 0)
                    ((totalSpent / budget.totalBudget) * 100).toInt().coerceIn(0, 100)
                else 0

                progressBudget.progress = progress
            }

            override fun onCancelled(error: DatabaseError) {
                toast("Failed to load expenses")
            }
        }

        expenseQuery.addValueEventListener(expenseListener)

        budgetListeners[budget.id] = Pair(expenseListener, memberListener)
        expenseQueryRefs[budget.id] = expenseQuery

        btnAddMember.setOnClickListener { showAddMemberDialog(budget.id) }
        btnAddExpense.setOnClickListener { showAddExpenseDialog(budget.id) }

        container.addView(card)
    }

    // Remove listeners before reload
    private fun detachAllChildListeners() {
        for ((id, listeners) in budgetListeners) {
            expenseQueryRefs[id]?.removeEventListener(listeners.first)
            membersRef.child(id).removeEventListener(listeners.second)
        }
        budgetListeners.clear()
        expenseQueryRefs.clear()
    }

    // Member row UI
    private fun createMemberRow(
        name: String,
        role: String,
        budgetId: String?,
        memberId: String?
    ): LinearLayout {

        val dp = resources.displayMetrics.density
        val dp8 = (8 * dp).toInt()
        val dp12 = (12 * dp).toInt()
        val dp40 = (40 * dp).toInt()

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(0, dp8, 0, dp8)
        row.gravity = android.view.Gravity.CENTER_VERTICAL

        val avatar = TextView(this)
        avatar.text = if (name.isNotEmpty()) name.first().toString() else "?"
        avatar.textSize = 16f
        avatar.setTextColor(0xFFFFFFFF.toInt())
        avatar.setBackgroundColor(0xFF6366F1.toInt())

        val avatarParams = LinearLayout.LayoutParams(dp40, dp40)
        avatarParams.marginEnd = dp12
        avatar.layoutParams = avatarParams

        val nameCol = LinearLayout(this)
        nameCol.orientation = LinearLayout.VERTICAL
        nameCol.layoutParams =
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        val tvName = TextView(this)
        tvName.text = name
        tvName.textSize = 14f
        tvName.setTextColor(0xFFFFFFFF.toInt())

        val tvRole = TextView(this)
        tvRole.text = role
        tvRole.textSize = 12f
        tvRole.setTextColor(0xFF9CA3AF.toInt())

        nameCol.addView(tvName)
        nameCol.addView(tvRole)

        row.addView(avatar)
        row.addView(nameCol)

        if (budgetId != null && memberId != null) {
            val delete = TextView(this)
            delete.text = "🗑"
            delete.setOnClickListener {
                confirmDeleteMember(budgetId, memberId, name)
            }

            row.setOnLongClickListener {
                confirmDeleteMember(budgetId, memberId, name)
                true
            }

            row.addView(delete)
        }

        return row
    }

    // Expense row UI
    private fun createExpenseRow(exp: SharedExpense): LinearLayout {

        val dp8 = (8 * resources.displayMetrics.density).toInt()

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.setBackgroundColor(0xFF1E293B.toInt())
        row.setPadding(dp8, dp8, dp8, dp8)

        val icon = TextView(this)
        icon.text = "📄"
        icon.textSize = 20f

        val tvDesc = TextView(this)
        tvDesc.text = exp.description
        tvDesc.setTextColor(0xFFFFFFFF.toInt())

        val tvAmount = TextView(this)
        tvAmount.text = "R%.2f".format(exp.amount)

        val delete = TextView(this)
        delete.text = "🗑"
        delete.setOnClickListener {
            expensesRef.child(exp.id).removeValue()
                .addOnSuccessListener { toast("Deleted") }
                .addOnFailureListener { toast("Failed") }
        }

        row.addView(icon)
        row.addView(tvDesc)
        row.addView(tvAmount)
        row.addView(delete)

        return row
    }

    // Add budget
    private fun showAddBudgetDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_budget, null)

        val name = view.findViewById<EditText>(R.id.tvBudgetName)
        val amt = view.findViewById<EditText>(R.id.tvBudgetAmount)

        val dialog = AlertDialog.Builder(this).setView(view).create()

        view.findViewById<Button>(R.id.btnSaveBudget).setOnClickListener {
            val n = name.text.toString().trim()
            val a = amt.text.toString().toDoubleOrNull()

            if (n.isEmpty() || a == null) {
                toast("Invalid input")
                return@setOnClickListener
            }

            val id = budgetsRef.push().key ?: return@setOnClickListener
            val budget = SharedBudget(id, n, a)

            budgetsRef.child(id).setValue(budget)
                .addOnSuccessListener { dialog.dismiss() }
                .addOnFailureListener { toast("Failed") }
        }

        dialog.show()
    }

    // Add expense
    private fun showAddExpenseDialog(budgetId: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense, null)

        val desc = view.findViewById<EditText>(R.id.etExpenseDesc)
        val amt = view.findViewById<EditText>(R.id.etExpenseAmount)

        val dialog = AlertDialog.Builder(this).setView(view).create()

        view.findViewById<Button>(R.id.btnSaveExpense).setOnClickListener {
            val d = desc.text.toString().trim()
            val a = amt.text.toString().toDoubleOrNull()

            if (d.isEmpty() || a == null) {
                toast("Invalid input")
                return@setOnClickListener
            }

            val id = expensesRef.push().key ?: return@setOnClickListener
            val exp = SharedExpense(id, budgetId, d, a)

            expensesRef.child(id).setValue(exp)
                .addOnSuccessListener { dialog.dismiss() }
                .addOnFailureListener { toast("Failed") }
        }

        dialog.show()
    }

    // Add member
    private fun showAddMemberDialog(budgetId: String) {
        val layout = LinearLayout(this)

        val et = EditText(this)
        et.hint = "Name"

        val btn = Button(this)
        btn.text = "Add"

        layout.addView(et)
        layout.addView(btn)

        val dialog = AlertDialog.Builder(this).setView(layout).create()

        btn.setOnClickListener {
            val name = et.text.toString().trim()
            if (name.isEmpty()) {
                toast("Enter name")
                return@setOnClickListener
            }

            val id = membersRef.child(budgetId).push().key ?: return@setOnClickListener
            val member = SharedMember(id, name, "Member")

            membersRef.child(budgetId).child(id).setValue(member)
                .addOnSuccessListener {
                    toast("Added")
                    dialog.dismiss()
                }
                .addOnFailureListener { toast("Failed") }
        }

        dialog.show()
    }

    // Remove member
    private fun confirmDeleteMember(budgetId: String, memberId: String, name: String) {
        AlertDialog.Builder(this)
            .setTitle("Remove")
            .setMessage("Remove $name?")
            .setPositiveButton("Yes") { _, _ ->
                membersRef.child(budgetId).child(memberId).removeValue()
            }
            .setNegativeButton("No", null)
            .show()
    }

    // Bottom nav
    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.shared

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> startActivity(Intent(this, HomePage::class.java))
                R.id.balance -> startActivity(Intent(this, BalancesActivity::class.java))
                R.id.expenses -> startActivity(Intent(this, ExpensesActivity::class.java))
                R.id.analytics -> startActivity(Intent(this, AnalyticsActivity::class.java))
            }
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        detachAllChildListeners()
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}