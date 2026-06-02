Overview:

Budget Budgie is an Android budgeting application created with Android Studio in  Kotlin. Through interactive charts and graphs, the app enables users to set budget objectives, manage numerous accounts, keep track of their own spending, and see their spending trends. The UI is intended to be straightforward, and aesthetically pleasing. The goal of the name and mascot, a cartoon budgie bird, is to make budgeting seem less daunting and more fun.


Purpose of the application:

Many people find it difficult to manage their own finances, particularly students. The majority of people overspend because they don't know where their money is going, not because they don't have enough. This is resolved by Budget Budgie by:

1.Providing users with a comprehensive picture of their overall balance across several accounts

2.Allowing users to document each expense with a description, category, date, time, and optional photo

3.Providing end users with graphic summaries of their expenditures so that trends can be easily identified

4.Giving users the option to choose minimum and maximum monthly spending targets

5.Using a gamification rewards system to encourage using the application.


2 of our own features :


Dashboard customisation


The user can choose what appears on their home screen thanks to this functionality. Budget Status, Total Spent, Total Expenses, Quick Actions, Recent Expenses, and Rewards are  six selectable widgets on the home screen. Every widget on the Dashboard Settings page has a checkbox next to it. A widget does not appear on the home screen if the user unchecks it and then hits Save.  The widget returns if the user checks it  and hits save. The user's changes remain even after closing the application and then opening it again as the options are stored using Android SharedPreferences.  


Shared budgets:

This feature lets a user create a named budget with a set total amount for example "Ryan's Birthday" with a budget of R1500. Once created it appears as a card on the Shared Budgets page showing how much has been spent and how much still remains. A member can be added to a budget . Any member can tap "Add Expense" on the budget card and enter a description and amount this  updates the spent and remaining totals . Individual expenses can also be deleted which subtracts them back from the total. this allows for a group of people such as roommates splitting rent costs or friends planning a trip together can all add their contributions to one shared budget and everyone can see the  total.


Design considerations :

The application uses a dark navy color palette which is used to maintain a modern and professional look and feel. All headings are bold and body texts are smaller for comfortable reading on small screens. Hints are in grey in order to suggest that they are there to guide the user.  The application uses a bottom navigation bar with 5 tabs: Home, balances, expenses, shared and analytics.  This was used as it allows users to quickly tap an icon on the navigation bar and access any section of the application which  was also used in the researched application such as PocketSmith,Buddy and GoodBudget.The app is designed to handle user input carefully . All screens validate what the user enters and display error messages . When a user completes a successful action such as logging in or registering an account, a brief Toast message appears at the bottom of the screen to confirm that the action was done. Before any data is  removed, such as logging out, a confirmation dialog appears first to make sure the user intended to do so and did not tap the button by accident. The Budget Budgie icon image also appears on both the login and register screens, which helps establish the app's identity and gives the entry screens a friendly and welcoming feel from the moment the user opens the app.


GitHub :

All code is committed and pushed to the master branch.  Each team member cloned the repository, added their code and then pushed their work. Before pushing, each member pulled the latest changes to avoid conflicts.


GitHub actions were used to run unit tests automatically and build the APK whenever code is pushed to the repository.  This is done to make sure that any code that is added does not cause the application to crash. It also ensures that the app works properly on other machines and not just the developers computer.



Youtube link :
