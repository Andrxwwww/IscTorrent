# IscTorrent

**A lightweight local project for simulating a distributed file-sharing system using P2P architecture.**

##  Table of Contents
- [Overview](#overview)
- [Features](#features)
  - [Core Features](#core-features)
- [Requirements](#requirements)
- [How to Run](#how-to-run)

---

##  Overview
IscTorrent is a simple yet effective tool to simulate and test peer-to-peer (P2P) file-sharing between multiple nodes running locally. Each node acts both as a client and a server, making it possible to exchange files without the need for a central authority. This is especially useful for understanding and experimenting with distributed systems.

---

##  Features

### Core Features
- **Peer-to-Peer Communication**: Nodes interact directly without relying on a central server.
- **File Search & Download**: Browse files across the network and download them from connected peers.
- **Manual Connections**: Easily link nodes by entering the appropriate port through the interface.

---

## Requirements

Before running the app, ensure you have the following installed:

- **Java Development Kit (JDK)**  
  - Version: 11 or higher  
  - How to check:  
    ```bash
    java -version
    ```
  - [Download JDK](https://openjdk.org/install/)

- **Git** *(optional but recommended for cloning the repository)*  
  - How to check:  
    ```bash
    git --version
    ```
  - [Download Git](https://git-scm.com/downloads)

- **Terminal or Shell Interface**  
  - Any standard terminal that supports `.bat` files (depending on your OS [ yes i'm sorry for not using linux :( ]).

---

## How to Run

1. Open a terminal (Command Prompt, PowerShell, or other shell).
2. Navigate to the root directory of the project.
3. Run the following command:
   ```bash
   ./RunProject.bat 1 2 3 [N]
[N] - > is the identifier of each node
---

## Folder Structure

Ensure you maintain the default folder layout. This is essential for the correct functioning of the system.
