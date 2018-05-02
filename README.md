# Shortcut Messenger

Shortcut Messenger is a decentralised (P2P) instant messaging application developed for my final year project (CS3IP16) at the University of Reading. It is written in Java and implemented from scratch using JoshuaKisson's Kademlia implementation as a reference https://github.com/JoshuaKissoon/Kademlia. It uses a modified implemention of the Kadmelia DHT protocol in order to discover and communicate with other nodes on the network. A significant amount of time was spent researching and implementing NAT (Network Address Translation) traversal techniques to enable to the application to function properly over the internet.

It comes in two flavours, a CLI and GUI. Executable JAR archives for both varieties can be found in the /target folder. Native packages for the current built, if available can be found in the /target/jfx/native folder.

Anyone thinking of using this project should be aware that it was created only as an academic proof of concept and is not by any means complete. There is a long list of known issues that I simply have run out of time to fix. I can give no guarantees of functionality or security so use it at your own risk. I will hopefully update this repository with more information when I have more time on my hands.

The project and its accompanying report were awarded the Sullivan prize for the highest computer science project mark at the University of Reading. I'm willing to share my project report and documentation to interested parties on a case by case basis. Please email me at rhysaled.streefland@ntlworld.com if you'd like a copy.

Full credits for this application can be found here: https://rstreefland.github.io/shortcutmessengerweb/ 
