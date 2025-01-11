# ServerList: A Central Server & Utilities for an abandoned MW3 Client

This project implements a 'central' server for the now-defunct MW3 modded client known as Tekno.  

The project was initially created in 2021 as a way to explore socket management in Java, a topic I studied at university. Two years ago, I refactored the code to simplify it, and more recently, I added new features such as logging and health monitoring for the central server.  

Interestingly, the original 'master' server for Tekno was written in just 20 lines of Python code. This highlights the complexity of working with Java. However, this project aims to go beyond the basics by introducing additional features, such as a self-cleaning thread to automatically remove servers from the list if they contained "crasher" strings. While this feature was part of the 2021 version, it is not included in this newer version due to time constraints. Additionally, this version lacks unit testing.  

## Features

- **Central Server**: Manages and lists servers for the Tekno MW3 client.  
- **Server Pinger**: Fetches and dumps information from various servers currently listed on the central server.  
- **Central Server Pinger**: Retrieves and displays basic information about the central server.  
