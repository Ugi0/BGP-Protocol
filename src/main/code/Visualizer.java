package main.code;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Visualizer {

    private char[][] intro;
    private char[][] mappingVisuals;
    private int[][] centerOfBox;

    public Visualizer(HashMap<Integer, List<Integer>> connections){
        int totalColumns = Integer.max(100, connections.size()*14);
        int totalRows = Integer.max(20, connections.size()*4);
        

        intro = bgpSimulation(totalColumns);

        nodeMap(connections, totalColumns, totalRows);
    }

    // Prints BGP SIMULATION in big letters
    public void printIntro(){
        printMatrix(intro);
    };

    public void printMap(){
        printMatrix(mappingVisuals);
    };

    public void shutRouter(int routerNumber){
        for(int i = 0; i < centerOfBox.length; i++){
            if (routerNumber == centerOfBox[i][0]){
                mappingVisuals[centerOfBox[i][1]][centerOfBox[i][2]] = ' ';
                /*mappingVisuals[centerOfBox[i][1]-1][centerOfBox[i][2]+1] = 'X';
                mappingVisuals[centerOfBox[i][1]+1][centerOfBox[i][2]-1] = 'X';
                mappingVisuals[centerOfBox[i][1]+1][centerOfBox[i][2]+1] = 'X';
                mappingVisuals[centerOfBox[i][1]-1][centerOfBox[i][2]-1] = 'X';*/
                break;
            } else if (routerNumber == 10 && centerOfBox[i][0] == 0){
                mappingVisuals[centerOfBox[i][1]][centerOfBox[i][2]] = ' ';
                mappingVisuals[centerOfBox[i][1]][centerOfBox[i][2]-1] = ' ';
                break;
            }
            
        };
    };

    //logic for mapping router boxes to matrix and drawing lines between them, lines cannot cross boxes, but can cross other lines
    private void nodeMap(HashMap<Integer, List<Integer>> connections, int totalColumns, int totalRows){
        
        char[][][] boxes = new char[connections.size()][][]; //for storing all the boxes
        char[][] completeMatrix = new char[totalRows][totalColumns]; //The final matrix to be printed

        //creating boxes of all routers in .config
        int index = 0;
        for (Map.Entry<Integer, List<Integer>> entry : connections.entrySet()) {
            boxes[index] = asNode(entry.getKey());
            index++;
        }

        boolean Finished = false;
        //Loop will repeat until no boxes are crossed with lines
        while (!Finished) {

            // Initiating empty values in matrix
            for (int i = 0; i < completeMatrix.length; i++) {
                for (int j = 0; j < completeMatrix[i].length; j++) {
                    completeMatrix[i][j] = ' ';
                }
            }

            centerOfBox = new int[connections.size()][3];

            // insert boxes into the matrix matrix
            for (int i = 0; i < connections.size(); i++) { 

                boolean placed = false;
                //Loops until box is successfully placed
                while (!placed) {
                    // Generate random position for the box
                    int startRow = new Random().nextInt(completeMatrix.length - boxes[i].length); // Ensure it fits
                    int startCol = new Random().nextInt(completeMatrix[0].length - boxes[i][0].length); // Ensure it fits

                    // Check for overlap
                    if (canPlaceBox(completeMatrix, boxes[i], startRow, startCol)) {
                        insertBox(completeMatrix, boxes[i], startRow, startCol);
                        placed = true;
                        centerOfBox[i][0] = boxes[i][1][2] - '0'; // box number and logging the center position of boxes
                        centerOfBox[i][1] = startRow + 1; 
                        centerOfBox[i][2] = startCol + 2; 
                    }
                }
            }
            // Drawing connections between boxes
            Finished = true; //Assume finish, will set to false if a collision occurs
            for(int i = 0; i < centerOfBox.length; i++){
                int boxToGet = centerOfBox[i][0];
                if (i==9){
                    boxToGet = 10;
                }
               
                for (int connectedBox : connections.get(boxToGet)){
                    //System.out.println("current box... "+connectedBox);
                    int[] connectedCoordinates = new int[2];
                    for (int[] row : centerOfBox) { //Checks center coordinates of target box
                        //System.out.println("current row..." + row[0]);
                        if (row[0] == connectedBox) {
                            connectedCoordinates[0] = row[1];
                            connectedCoordinates[1] = row[2];
                            break;
                        }
                        if (row[0] == 0){
                            connectedCoordinates[0] = row[1];
                            connectedCoordinates[1] = row[2];
                            break;
                        }
                    }

                    int startY = centerOfBox[i][1];
                    int startX = centerOfBox[i][2];
                    int endY = connectedCoordinates[0];
                    int endX = connectedCoordinates[1];

                    if (startX<endX){
                        startX++;
                        endX--;
                    }
                    if (startX>endX){
                        startX--;
                        endX++;
                    }
                    if (startY<endY){
                        startY++;
                        endY--;
                    }
                    if (startY>endY){
                        startY--;
                        endY++;
                    }


                    Finished = drawConnection(completeMatrix, startY, startX, endY, endX);
                }
                if (!Finished) {
                    break; // Exit if collision
                }
            }
        }

        mappingVisuals=completeMatrix;

    }

    //Adds box to matrix
    private void insertBox(char[][] matrix, char[][] box, int startRow, int startCol) {
        for (int i = 0; i < box.length; i++) {
            for (int j = 0; j < box[i].length; j++) {
                matrix[startRow + i][startCol + j] = box[i][j];
            }
        }
    }

    //Checks for overlap of boxes
    private boolean canPlaceBox(char[][] matrix, char[][] box, int startRow, int startCol) {
        for (int i = 0; i < box.length; i++) {
            for (int j = 0; j < box[i].length; j++) {
                if (matrix[startRow + i][startCol + j] != ' ') {
                    return false;
                }
            }
        }
        return true;
    }

    //draws connection in matrix
    private static boolean drawConnection(char[][] matrix, int startX, int startY, int endX, int endY) {
        return recursiveSplit(startX, startY, endX, endY, matrix);
    }

    //Finds centerpoint of two boxes and fills in the value from there, splitting into smaller halves until the entire path is drawn
    //Could be made better, working on it.
    private static boolean recursiveSplit(int startX, int startY, int endX, int endY, char[][] matrix) {
        if (Math.abs(endX - startX) <= 1 && Math.abs(endY - startY) <=1) {  // Base case: no more splitting possible
            if (matrix[startX][startY] == ' ' || matrix[startX][startY] == '-' || matrix[startX][startY] == '|'){
                matrix[startX][startY] = '@';
            } 
            if (matrix[endX][endY] == ' ' || matrix[endX][endY] == '-' || matrix[endX][endY] == '|'){
                matrix[endX][endY] = '@';
            } 
            return true;
        }

        int[] mid = new int[]{(startX+endX)/2, (startY+endY)/2};
        if (matrix[mid[0]][mid[1]] == ' ' || matrix[mid[0]][mid[1]] == '*' || matrix[mid[0]][mid[1]] == '-' ){
            matrix[mid[0]][mid[1]] = '*';
        } else return false;

        // Recur for the left and right halves
        return recursiveSplit(startX, startY, mid[0], mid[1], matrix) && recursiveSplit(mid[0], mid[1], endX, endY, matrix);
    }

    //Makes the 'boxes' for the routers
    private char[][] asNode(int number){
        String numberString = Integer.toString(number); 
        char numberChar = numberString.charAt(0); // converting int to char
        char[][] box;
        if (number<10){
            box = new char[][]{
            {' ','-', '-', '-', ' '},
            {'|',' ', numberChar , ' ', '|'},
            {' ', '-', '-', '-',' '},
            };
        }else{
            box = new char[][]{
                {' ','-', '-', '-', ' '},
                {'|','1', '0', ' ', '|'},
                {' ', '-', '-', '-',' '},
            };
        }
        return box;
    }

    //Visualization "header"
    private char[][] bgpSimulation(int totalColumns){
        char[][] letters = {
            {'B', 'B', 'B', 'B', ' ', ' ', 'G', 'G', 'G', 'G', ' ', ' ', 'P', 'P', 'P', 'P', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'S', 'S', ' ', ' ', 'I', ' ', 'M', ' ', ' ', ' ', 'M', ' ', 'U', ' ', ' ', 'U', ' ', 'L', ' ', ' ', ' ', ' ', ' ', 'A', 'A', ' ', ' ', 'T', 'T', 'T', 'T', 'T', ' ','I', ' ', ' ', 'O', 'O', ' ', ' ','N', ' ', ' ', ' ', 'N'},
            {'B', ' ', ' ', ' ', 'B', ' ', 'G', ' ', ' ', ' ', ' ', ' ', 'P', ' ', ' ', ' ', 'P', ' ', ' ', ' ', ' ', ' ', 'S', ' ', ' ', 'S', ' ', 'I', ' ', 'M', 'M', ' ', 'M', 'M', ' ', 'U', ' ', ' ', 'U', ' ', 'L', ' ', ' ', ' ', ' ', 'A', ' ', ' ', 'A', ' ', ' ', ' ', 'T', ' ',' ', ' ', 'I', ' ', 'O', ' ', ' ', 'O', ' ','N', 'N', ' ', ' ', 'N'},
            {'B', ' ', ' ', ' ', 'B', ' ', 'G', ' ', ' ', ' ', ' ', ' ', 'P', ' ', ' ', ' ', 'P', ' ', ' ', ' ', ' ', ' ', ' ', 'S', ' ', ' ', ' ', 'I', ' ', 'M', ' ', 'M', ' ', 'M', ' ', 'U', ' ', ' ', 'U', ' ', 'L', ' ', ' ', ' ', ' ', 'A', ' ', ' ', 'A', ' ', ' ', ' ', 'T', ' ',' ', ' ', 'I', ' ', 'O', ' ', ' ', 'O', ' ','N', ' ', 'N', ' ', 'N'},
            {'B', 'B', 'B', 'B', ' ', ' ', 'G', ' ', 'G', 'G', ' ', ' ', 'P', 'P', 'P', 'P', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'S', ' ', ' ', 'I', ' ', 'M', ' ', 'M', ' ', 'M', ' ', 'U', ' ', ' ', 'U', ' ', 'L', ' ', ' ', ' ', ' ', 'A', 'A', 'A', 'A', ' ', ' ', ' ', 'T', ' ',' ', ' ', 'I', ' ', 'O', ' ', ' ', 'O', ' ','N', ' ', 'N', ' ', 'N'},
            {'B', ' ', ' ', ' ', 'B', ' ', 'G', ' ', ' ', 'G', ' ', ' ', 'P', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'S', ' ', 'I', ' ', 'M', ' ', ' ', ' ', 'M', ' ', 'U', ' ', ' ', 'U', ' ', 'L', ' ', ' ', ' ', ' ', 'A', ' ', ' ', 'A', ' ', ' ', ' ', 'T', ' ',' ', ' ', 'I', ' ', 'O', ' ', ' ', 'O', ' ','N', ' ', 'N', ' ', 'N'},
            {'B', ' ', ' ', ' ', 'B', ' ', 'G', ' ', ' ', 'G', ' ', ' ', 'P', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'S', ' ', ' ', 'S', ' ', 'I', ' ', 'M', ' ', ' ', ' ', 'M', ' ', 'U', ' ', ' ', 'U', ' ', 'L', ' ', ' ', ' ', ' ', 'A', ' ', ' ', 'A', ' ', ' ', ' ', 'T', ' ',' ', ' ', 'I', ' ', 'O', ' ', ' ', 'O', ' ','N', ' ', ' ', 'N', 'N'},
            {'B', 'B', 'B', 'B', ' ', ' ', 'G', 'G', 'G', 'G', ' ', ' ', 'P', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'S', 'S', ' ', ' ', 'I', ' ', 'M', ' ', ' ', ' ', 'M', ' ', ' ', 'U', 'U', ' ', ' ', 'L', 'L', 'L', 'L', ' ', 'A', ' ', ' ', 'A', ' ', ' ', ' ', 'T', ' ',' ', ' ', 'I', ' ', ' ', 'O', 'O', ' ', ' ','N', ' ', ' ', ' ', 'N'}
        };

        //Size of visualization header
        int totalRows = letters.length + 4;
        char[][] bgpMatrix = new char[totalRows][totalColumns];

        //Default value for a char in matrix is ' '
        for (int i = 0; i < totalRows; i++) {
            for (int j = 0; j < totalColumns; j++) {
                bgpMatrix[i][j] = ' ';
            }
        }

        // Top and bottom borders
        for (int j = 0; j < totalColumns; j++) {
            bgpMatrix[0][j] = '/';
            bgpMatrix[totalRows - 1][j] = '/';
        }

        // Left and right borders
        for (int i = 1; i < totalRows - 1; i++) {
            bgpMatrix[i][0] = '|';
            bgpMatrix[i][totalColumns - 1] = '|';
        }

        //"BGP SIMULATION" into the center of the matrix
        int startRow = (totalRows - letters.length)/2;
        int startCol = (totalColumns - letters[0].length)/2;

        for (int i = 0; i < letters.length; i++) {
            for (int j = 0; j < letters[i].length; j++) {
                bgpMatrix[startRow + i][startCol + j] = letters[i][j];
            }
        }
        return bgpMatrix;
    }

    // function for printing a matrix
    private static void printMatrix(char[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j]);
            }
            System.out.println();
        }
    }
}