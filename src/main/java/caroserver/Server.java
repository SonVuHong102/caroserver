/*
 * CODE,
 * CODE NUA,
 * CODE MAI...
 */
package caroserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import utils.Value;

/**
 *
 * @author Son Vu
 */
public class Server {

    private HashMap<String, ClientListener> clientMap;
    private ArrayList<int[][]> boardList;
//    private int[][] boardList;

    public void startServer() {
        ServerSocket server = null;
        try {
            // Create TCP Socket Server
            server = new ServerSocket(Value.serverPort);

            clientMap = new HashMap<String, ClientListener>();
            boardList = new ArrayList<int[][]>();
            // Listening for client
            System.out.println("$ Server Created");
            while (true) {
                Socket client = server.accept();
                String name = "_client" + Value.clientOrdinalNum;
                Value.clientOrdinalNum++;
                System.out.println("\nClient : " + client.getInetAddress().getHostAddress() + " - " + client.getPort()
                        + " connected as " + name);
                ClientListener newClient = new ClientListener(client, name);
                newClient.start();
                clientMap.put(name, newClient);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        // TODO On Server Closing : ¯\_(ツ)_/¯
    }

// Send Refresh to all
    private void sendRefreshToAll() {
        StringBuilder sb = new StringBuilder("RefreshPlayer ");
        StringBuilder players = new StringBuilder("");
        for (String i : clientMap.keySet()) {
            if (!i.contains("_client")) {
                players.append(i + " ");
            }
        }
        sb.append(players);
        for (ClientListener cl : clientMap.values()) {
            cl.sendRefresh(sb.toString());
        }
    }

    public class ClientListener implements Runnable {

        private Socket client;
        private String clientName;
        private DataInputStream fromClient;
        private DataOutputStream toClient;

        private int boardID;
        private int side;

        public ClientListener(Socket client, String clientName) throws IOException {
            this.client = client;
            this.clientName = clientName;
            fromClient = new DataInputStream(client.getInputStream());
            toClient = new DataOutputStream(client.getOutputStream());
        }

        private Thread worker;
        private final AtomicBoolean running = new AtomicBoolean(false);

        public void start() {
            worker = new Thread(this);
            worker.start();
        }

        public void stop() {
            clientMap.remove(clientName);
            sendRefreshToAll();
            running.set(false);
        }

        public void sendToClient(String msg) {
            try {
                toClient.writeUTF(msg);
                System.out.println("Send to [" + clientName + "] : " + msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void closeSocket() {
            try {
                fromClient.close();
                toClient.close();
                client.close();
                clientMap.remove(clientName);
                sendRefreshToAll();
                this.stop();
                System.out.println("$ " + clientName + " has disconnected");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void checkLogin(String username, String password) {
            if (clientMap.containsKey(username)) {
                sendToClient("Login InUsing");
                return;
            }
            ClientDAO userDb = new ClientDAO();
            if (userDb.checkLogin(username, password)) {
                sendToClient("Login Accepted");
                clientMap.put(username, this);
                clientMap.remove(clientName);
                clientName = username;
            } else {
                sendToClient("Login Rejected");
            }
        }

        private void checkSignup(String username, String password, String repassword) {
            if (!password.equals(repassword)) {
                sendToClient("Signup " + "PasswordNotMatch");
            } else {
                ClientDAO userDb = new ClientDAO();
                if (userDb.isExistedUsername(username)) {
                    sendToClient("Signup " + "UsernameIsExisted");
                } else if (userDb.addClient(new Client(username, password, 0))) {
                    sendToClient("Signup " + "Successed");
                } else {
                    sendToClient("Signup " + "Failed");
                }
            }
        }

        public void sendRefresh(String players) {
            sendToClient(players);
        }

        private void sendInvitation(String other) {
            if (clientMap.containsKey(other)) {
                side = 1;
                clientMap.get(other).sendToClient("Invitation " + clientName);
                sendToClient("InvitedPlayer " + other);
            } else {
                sendToClient("InvitedPlayerFailed " + other);
            }
        }

        private void sendRejected(String other) {
            clientMap.get(other).sendToClient("RejectedInvitation " + clientName);
        }

        private void sendAccepted(String opp) {
            if (clientMap.containsKey(opp) && clientMap.containsKey(clientName)) {
                side = -1;
                clientMap.get(opp).sendToClient("AcceptedInvitation " + clientName);
            } else {
                sendToClient("PlayerNotAvaiable " + opp);
            }
        }

        private void sendCreateGame(String opp) {
            if (clientMap.containsKey(opp) && clientMap.containsKey(clientName)) {
                int[][] board = new int[Value.blockNum][Value.blockNum];
                boardList.add(board);
                boardID = (boardList.size() - 1);
                sendToClient("CreateGame " + opp + " 1 " + boardID);
                clientMap.get(opp).sendToClient("CreateGame " + clientName + " -1 " + boardID);
            } else {
                sendToClient("PlayerNotAvaiable " + opp);
            }
        }

        private void sendExitToOpp(String opp) {
            clientMap.get(opp).sendToClient("ExitedGame " + clientName);
        }

        private void sendInPlaying(String opp) {
            clientMap.get(opp).sendToClient("InPlaying " + clientName);
        }

        private void sendExit(String opp) {
            sendToClient("ExitedGame " + opp);
        }
        //checkwin
//        public boolean checkWin(int i, int j, int[][] boardStatus) {
//        	System.out.println("isStartCheckingWin");
//            int d = 0, k = i, h;
//            // check row
//            while (boardStatus[k][j] == boardStatus[i][j]) {
//                d++;
//                k++;
//            }
//            k = i - 1;
//            while (boardStatus[k][j] == boardStatus[i][j]) {
//                d++;
//                k--;
//            }
//            if (d > 4) {
//                return true;
//            }
//            d = 0;
//            h = j;
//            // check col
//            while (boardStatus[i][h] == boardStatus[i][j]) {
//                d++;
//                h++;
//            }
//            h = j - 1;
//            while (boardStatus[i][h] == boardStatus[i][j]) {
//                d++;
//                h--;
//            }
//            if (d > 4) {
//                return true;
//            }
//            // check diagonal 1
//            h = i;
//            k = j;
//            d = 0;
//            while (boardStatus[i][j] == boardStatus[h][k]) {
//                d++;
//                h++;
//                k++;
//            }
//            h = i - 1;
//            k = j - 1;
//            while (boardStatus[i][j] == boardStatus[h][k]) {
//                d++;
//                h--;
//                k--;
//            }
//            if (d > 4) {
//                return true;
//            }
//            // check diagonal 2
//            h = i;
//            k = j;
//            d = 0;
//            while (boardStatus[i][j] == boardStatus[h][k]) {
//                d++;
//                h++;
//                k--;
//            }
//            h = i - 1;
//            k = j + 1;
//            while (boardStatus[i][j] == boardStatus[h][k]) {
//                d++;
//                h--;
//                k++;
//            }
//            System.out.println("isEndCheckingWin");
//            if (d > 4) {
//                return true;
//            }
//            // nếu không đương chéo nào thỏa mãn thì trả về false.
//            return false;
//        }
        private int checkWin(int row, int col, int[][] matrix) {
    		int[][] go = { { 0, -1, 0, 1 }, { -1, 0, 1, 0 }, { 1, -1, -1, 1 },
    				{ -1, -1, 1, 1 } };
    		int i = row, j = col;

                    // matrix[row][col] = 1 => team 1 thắng
                    // matrix[row][col] = 2 => team 2 thắng.
    		for (int direction = 0; direction < 4; direction++) {
    			int count = 0;
    			i = row;
    			j = col;
                            // đi xuôi
    			while (i >=0 && i < matrix.length && j >=0 && j < matrix.length
    					&& matrix[i][j] == matrix[row][col]) {
    				count++;
    				if (count >= 5) {
    					return matrix[row][col];
    				}
    				i += go[direction][0];
    				j += go[direction][1];
    			}

                            // đi ngược
    			count--;
    			i = row;
    			j = col;
    			while (i >=0 && i < matrix.length && j >=0 && j < matrix.length
    					&& matrix[i][j] == matrix[row][col]) {
    				count++;
    				if (count >= 5) {
    					return matrix[row][col]; // thang
    				}
    				i += go[direction][2];
    				j += go[direction][3];
    			}
    		}
    		return 0; // khong thang
    	}

        // end checkwin
        private void sendMoveToOpp(String opp, String _row, String _column) {
            int row = Integer.parseInt(_row);
            int column = Integer.parseInt(_column);
            boardList.get(boardID)[row][column] = side;
            clientMap.get(opp).sendToClient("OppMoved " + row + " " + column);
          
            if (checkWin(row, column, boardList.get(boardID)) == side) {
                clientMap.get(opp).sendToClient("OppWon " + clientName);
                sendToClient("YouWon " + opp);
            }
        }

        private void sendChatToOpp(String opp, String msg) {
            clientMap.get(opp).sendToClient("Chat " + opp);
            clientMap.get(opp).sendToClient(msg);
        }

        private void sendSurrender(String opp) {
            clientMap.get(opp).sendToClient("OppSurrender " + opp);
        }

        public void run() {
            running.set(true);
            while (running.get()) {
                try {
                    String msg = fromClient.readUTF();
                    System.out.println("Receive from [" + clientName +"] : " + msg);
                    String[] t = msg.split(" ");
                    if (t[0].equals("ClosingSocket")) {
                        closeSocket();
                        // IN PLAYING GAME
                        if (t.length > 1) {
                            sendExitToOpp(t[1]);
                        }
                    } else if (t[0].equals("Login")) {
                        String username = t[1];
                        String password = t[2];
                        checkLogin(username, password);
                    } else if (t[0].equals("Signup")) {
                        String username = t[1];
                        String password = t[2];
                        String repassword = t[3];
                        checkSignup(username, password, repassword);
                    } else if (t[0].equals("RefreshPlayer")) {
                        sendRefreshToAll();
                    } else if (t[0].equals("Invite")) {
                        sendInvitation(t[1]);
                    } else if (t[0].equals("RejectedInvitation")) {
                        sendRejected(t[1]);
                    } else if (t[0].equals("AcceptedInvitation")) {
                        sendAccepted(t[1]);
                    } else if (t[0].equals("ReadyToPlay")) {
                        sendCreateGame(t[1]);
                    } else if (t[0].equals("ExitedGame")) {
                        sendExit(t[1]);
                    } else if (t[0].equals("InPlaying")) {
                        sendInPlaying(t[1]);
                    } else if (t[0].equals("OppWin")) {
                        sendExit(t[1]);
                    } else if (t[0].equals("TheyWin")) {
                        sendExit(t[1]);
                    } else if (t[0].equals("Move")) {
                        String opp = t[1];
                        String row = t[2];
                        String column = t[3];
                        sendMoveToOpp(opp, row, column);
                    } else if (t[0].equals("Chat")) {
                        msg = fromClient.readUTF();
                        sendChatToOpp(t[1], msg);
                    } else if (t[0].equals("Surrender")) {
                        sendSurrender(t[1]);
                    } else if (t[0].equals("InitRoomSession")) {
                        clientName = t[1];
                        clientMap.put(clientName, this);
                        sendRefreshToAll();
                    }

                } catch (Exception e) {
                    this.stop();
                }
            }
        }
    }
}
