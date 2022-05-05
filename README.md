# remtPlatform
Client/Server application that you can use to move file from the client to the server and vice versa. You can also run on the server evental file in remote server

The core of the application consists in a socket between a server and a client, that let them to share files, sending them to each other. The server has his own file 
system ( sort of database ), and the client can send and receive this files. Then, the client can also ask for the server to run a specific command, file, or whatever,
moving on the file system, and sending commands.

This piece of software is useful when you have a powerful machine and a less powerful one. You have to use the less powerful one ( maybe a laptop ), but you need much
more power. So, you let your projects run on a remote machine, that it your main machine.
