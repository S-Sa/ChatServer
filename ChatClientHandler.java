import java.io.*;
import java.net.*;
import java.util.*;

class ChatClientHandler extends Thread{
    Socket socket;
    BufferedReader in;
    BufferedWriter out;
    private List<ChatClientHandler> clients = new ArrayList<ChatClientHandler>(); //追加するオブジェクトの型をあらかじめ宣言しておく
    private String name; //自分自身の名前
    private List<ChatClientHandler> rejects = new ArrayList<ChatClientHandler>();
    public void run(){
	try{
	    open();
	    while(true){
		String message = recieve();
		String[] commands = message.split(" "); //空白で区切る
		if(commands[0].equalsIgnoreCase("post")){ //postと入力された場合
		    post(commands[1]);
		}else if(commands[0].equalsIgnoreCase("help")){ //helpと表示された場合
		    help();
		}else if(commands[0].equalsIgnoreCase("whoami")){ //whoamiと入力された場合
		    send("Your name is :" + getClientName()); //getClientnameで自分の名前を取得する
		}else if(commands[0].equalsIgnoreCase("name")){ //nameと入力された場合
		    changeName(commands[1]);
		}else if(commands[0].equalsIgnoreCase("user")){ //userと入力された場合
		    allclients();
		}else if(commands[0].equalsIgnoreCase("reject")){ //rejectと入力された場合
		    rejects(commands[1]);
		}
		if(message.equals("bye")){ //byeと入力された場合
		    break;
		}
		//send("[" + this.getClientName() + "]" + message);
		send(message);
	    }
	}catch(IOException e){
	    e.printStackTrace();
	}finally{
	    close();
	}
    }
    
    
    ChatClientHandler(Socket socket,List clients){
	this.socket = socket;
	this.clients = clients;
	this.name = "undefined" + (clients.size() + 1);
    }

    /*
      名前を取得
     */
    public String getClientName(){
	return name; //名前を返すだけ
    }

    /*
      ヘルプを表示
     */
    public void help()throws IOException{
	send("post message : このサーバに接続している全員にmessageの部分を送ります。");
	send("name message : クライアントの名前をmessageに変えます。");
	send("bye : このチャットサーバから退出し、終了します。");
	send("whoami : 自分の名前を確認できます。");
    send("reject : 指定した人のメッセージを受け取らないようにしまします。");
    }


    /*
      名前を変更
     */
    public void changeName(String name)throws IOException{
	List names = new ArrayList(); //名前を格納しておくリスト
	for(int i = 0;i < clients.size();i++){
	    ChatClientHandler handler = (ChatClientHandler)clients.get(i);
	    if(handler.name.equals(name)){ //全クライアントの名前と使用したい名前を確認
		send("その名前はすでに使用されています");
		return; //使用されていた場合、名前を変えずに関数を終了する
	    }
	}
	this.name = name; //このクライアントの名前を変更する
    }

    /*
      rejectします
    */
    public void rejects(String rejname)throws IOException{
        for(int i=0;i<clients.size();i++){
            for(int j = 0;j < rejects.size();j++){
                ChatClientHandler gatti = (ChatClientHandler)rejects.get(i);
                if(gatti.name.equals(rejname)){
                    rejects.remove(gatti);
                    send(rejname + "のrejectを削除しました");
                    return ;
                }
            }
            ChatClientHandler handler = (ChatClientHandler)clients.get(i);
            if(handler.name.equals(rejname)){
                rejects.add(handler);
                send(rejname + "をrejectしました");
                send("現在rejectした人の一覧です");
                for(int k = 0;k < rejects.size();k++){
                    ChatClientHandler rejuser = (ChatClientHandler)rejects.get(k);
                    out.write(rejuser.getClientName());
                    out.write(",");
                }
                send("");
                return ;
            }
        }
        send("指定された名前は存在しません");
    }


    /*
      全てのユーザを表示
     */
    public void allclients()throws IOException{
	//List allclients = clients;
	List<String> names  = new ArrayList<String>(); //string型で名前だけを保持しておくListを用意
	String user;
	
	//Collections.sort(clients);
	for(int i = 0;i < clients.size();i++){
	    ChatClientHandler handler = (ChatClientHandler)clients.get(i);
	    names.add(handler.getClientName()); //nameにclientsのリストにある名前の部分を取得
	}

	Collections.sort(names); //名前をソート
	
	for(int j = 0;j < names.size();j++){ //ソートした順番に出力
	    user = names.get(j);
	    out.write(user);
	    out.write(",");
	}
	
	out.write("\r\n"); //改行
	out.flush(); //バッファー内の全てを書き出し
    }


    /*
      参加しているメンバーにメッセージを送信
     */
    public void post(String message)throws IOException{
	List<String> names = new ArrayList<String>(); //Stringで格納するListを用意
    int flag = 0; //reject用
	for(int i = 0;i < clients.size();i++){
        flag = 0;
	    ChatClientHandler handler = (ChatClientHandler)clients.get(i);
        for(int j = 0;j < handler.rejects.size();j++){
            ChatClientHandler rejecter = (ChatClientHandler)handler.rejects.get(j);
            if(this == rejecter){
                flag = 1;
            }
            System.out.println(flag);
        }
        if((handler != this) && (flag == 0)){ //取得したhandlerが自分自身でない場合
            names.add(handler.getClientName());  //自分の名前を追加
            handler.send("["+ this.getClientName() + "]" + message); //自分の名前のヘッダーをつけてメッセージを発信
        }
	}
	Collections.sort(names); //名前をソート
	String returnMessage = "";
	for(int i = 0;i<names.size();i++){
	    returnMessage = returnMessage + names.get(i) + ",";
	}
	this.send(returnMessage);
    }

    void open() throws IOException{
	    InputStream socketIn = socket.getInputStream();
	    in = new BufferedReader(new InputStreamReader(socketIn));

	    OutputStream socketOut = socket.getOutputStream();
	    Writer streamWriter = 
	    out = new BufferedWriter(new OutputStreamWriter(socketOut));
	    send("使い方はhelpコマンドで確認できます。");
    }
    String recieve() throws IOException{
	String line = in.readLine();
	System.out.println(line);
	return line;
    }
    void send(String message)throws IOException{
	out.write(message);
	out.write("\r\n");
	out.flush(); //バッファー内の全てを書き出し
    }
    void close(){
	clients.remove(this);
	if(in != null){ try{ in.close();}catch(IOException e){}}
	if(out != null){ try{ out.close();}catch(IOException e){}}
	if(socket != null){ try{ socket.close();}catch(IOException e){}}
    }
}
