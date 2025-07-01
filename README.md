# java-swing-POS
a POS made for a bookstore in java swing backed by MySQL database 

HOW TO USE 

1-copy src,pom.xml and config.properties and past in your project folder 
you can name it BookStoreManagement for ease but if you want your own own name you may have to change package name in java codes 

2-dump the files from database folder in your database made on your machine 
triggers and structires are intact but procedure could'nt be included in dump so it may effect one or two features of app you may have to wirte those functions in code to get them work 
features

3-change user  seettings for your database according to the user info in config you can change config username and password if you want then proceed to MySQL 

4-Make sure your sever is live and run the java project it should show you a login screen 
credentials for both admin and developer are stored in mysql dump 
still here they are if you cant find them 
ROLE    username  password
admin     abc     	123	
developer dev    	dev123	


STRUCTURE

-each container got its own class for clarity
-functions are defined seprately for each process and called 

FEATURES

-bilingual(urdu and English)
-inventory
-sales
-purchases
-expense management 
-profit and loss calculater(will work after writing procedure in MySQL or function in java code)
-reports and invoice generation
-account book and transaction manager

REMINDER !!!
some features that are calling sql stored procedure may not work becasue stored procedure didnt included in dumps you may have to tweak your java code a little bit for that or rewrite the missing procedure . thank you
