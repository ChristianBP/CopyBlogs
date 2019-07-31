# CopyBlogs
Set up two instances of wordpress either locally or remotely. Instructions on how to do this can be found [here](https://wordpress.org/support/article/how-to-install-wordpress/).

Create a new json file at /cbp/copyBlogs/config.json

The contents will be:
  
{  
	  "sourceDbHost": "localhost",  
	  "sourceDbUrl": "jdbc:mariadb://localhost:3306/wordpress?zeroDateTimeBehavior=convertToNull&user={username}&password={password}",  
	  "sourceDbDriver": "org.mariadb.jdbc.Driver",  
	  "sourceDbSchemaUrl": "jdbc:mariadb://localhost:3306/information_schema?user={username}&password={password}",  
	  "sourceTablePrefix": "",  
	  "destinationDbHost": "localhost",  
	  "destinationDbUrl": "jdbc:mariadb://localhost:3306/wordpress?zeroDateTimeBehavior=convertToNull&user={username}&password={password}",  
	  "destinationDbDriver": "org.mariadb.jdbc.Driver",  
	  "destinationTablePrefix": "wp_",  
	  "pathToBeCopied": "/a_blog_path/",  
	  "remoteCopy": "false"  
}  
  
Where the blog is being transferred from the source database to the destination database and:  
DbHost is the hostname  
DbUrl is the jdbc connection string for the database and includes the username and password  
DbDriver is the Java driver required to connect to the database  
sourceDbSchemaUrl is the connection string to the source database schema  
TablePrefix is what all the wordpress tables are prefixed with in the database  
pathToBeCopied is the path being copied in both databases. It doesn't necessarily have to be in the destination database already but it will be overwritten if it is  
remoteCopy is whether or not the extra files for the databases are on the local machine or remote servers.  
  
  
Create a new json file at /cbp/copyBlogs/fileServerConfig.json  
  
If you are only copying files locally then the contents will be:  
  
{  
	"sourceAdditionalFilesPath" : "/usr/app/blogs/public/wp-content/blogs.dir/",  
	"destinationAdditionalFilesPath" : "/usr/app/blogs/public/wp-content/blogs.dir/",  
}  
  
Where:  
AdditionalFilesPath is the path to Wordpress's additional files  
  
If you are copying the files remotely then the contents will be:  
  
{  
	"sourceAdditionalFilesPath" : "/usr/app/blogs/public/wp-content/blogs.dir/",  
	"sourceFileServerUsername" : "{username}",  
	"sourceFileServer" : "localhost",  
	"sourceFileServerPort" : "22",  
	"intermediaryAdditionalFilesPath" : "C:/usr/app/blogs/public/wp-content/blogs.dir/",  
	"destinationAdditionalFilesPath" : "/usr/app/blogs/public/wp-content/blogs.dir/",  
	"destinationFileServerUsername" : "{username}",  
	"destinationFileServer" : "localhost",  
	"destinationFileServerPort" : "22"	  
}  
  
Where:  
AdditionalFilesPath is the path to Wordpress's additional files  
FileServerUsername is the username that has access to the files on the remote server  
FileServer is the server being used  
FileServerPort is the port to connect to the file server through  
intermediaryAdditionalFilesPath is a local filepath to temporarily store the files in while they are being copied  
  
As you can see this connection is not using a password. In order to accomplish this:  
  
If you do not already have an ssh key then follow the instructions [here](https://confluence.atlassian.com/bitbucketserver/creating-ssh-keys-776639788.html).  
  
Once you have an ssh key:  
1) Navigate to ~/.ssh or C:\Users\(you)\.ssh  
2) Open up id_rsa and copy its contents.  
3) Create a new directory called keys and a new file in that directory called copyBlogs.  
4) Paste the contents of id_rsa into copyBlogs.  
If you want to use a file other than ~/.ssh/keys/copyBlogs then just change the variable identityLocation in CopyFiles.java  
5) Open up id_rsa.pub and copy its contents.  
  
Repeat the following steps for both the 'from' file server and the 'to' file server.  
1) Open command prompt or terminal and ssh into the file server.  
2) Create ~/.ssh/authorized_keys if it doesn't exist.  
3) Copy the contents of id_rsa.pub into ~/.ssh/authorized_keys.  
  
You can verify that this has worked by logging onto each server, through the terminal, without a password.  
  
The last configuration file you will need is /cbp/copyBlogs/log4j.properties  
This can be configured however you want.  
Here is an example:  
  
\# Root logger  
log4j.rootLogger=info, stdout, file  
\# Log messages for stdout  
log4j.appender.stdout=org.apache.log4j.ConsoleAppender  
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout  
log4j.appender.stdout.layout.conversionPattern=%m%n  
\# Direct log messages to a log file  
log4j.appender.file=org.apache.log4j.RollingFileAppender  
log4j.appender.file.file=logs/copyBlogs.log  
log4j.appender.file.MaxFileSize=100MB  
log4j.appender.file.MaxBackupIndex=10  
log4j.appender.file.layout=org.apache.log4j.PatternLayout  
log4j.appender.file.layout.conversionPattern=%m%n  
  
Once you have these three configuration files setup then you are ready to run the application  
Either run the Main.java file from an IDE or compile and run the jar.  
