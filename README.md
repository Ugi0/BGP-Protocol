USER MANUAL

1. Change .config file
   - First thing to do in the .config file is to set how many routers you want to start and how fast you want the routers to timeout (in seconds). MAX amount of routers is 10
   - Decide which router connects to which router.
     - The logic under port connections is (router)-(connected router).
   - Each connection should start from the new line of the config file.
   
   Example:
   
   ![contents of .config](https://github.com/Ugi0/BGP-Protocol/blob/05dcfcb47cfbf2845063426d65fd8b06472ac36a/config_example.png)
   
3. Run Main
   - Running the Main.java file starts the simulator with the configured topology and builds the routing tables.
   - Setting the debug to true causes the simulator to print more specific information about the BGP messages and what is happening when the connections are formed and the routing tables updated,
     but having these logs on can cause it to be more difficult to use the commands.
  
     Debugging option in the beginning of Main class:
     
     ![Debugging option](https://github.com/Ugi0/BGP-Protocol/blob/05dcfcb47cfbf2845063426d65fd8b06472ac36a/debug_option.png)

4. There are six commands available when the program is running: 

      help: this prints a list of all the other commands. 

      get routing: this prints routing tables for all routers. 

      shutdown (number): this simulates the router (number) crashing. 

      shutdown gracefully (number): this is controlled shutdown of a router. The router notifies neighbours before shutting down.

      visualize: prints a visualization of the topology. 

      send (message) (source) (destination): allows you to manually send IP packets from a router to another router. 
