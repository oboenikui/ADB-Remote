package com.oboenikui.adbremote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPAddress {
    private String ipAddress = null;
    private int port = -1;
    public final static String IPV4_FORMAT = "(((1?[1-9]?[0-9])|(10[0-9])|(2[0-4][0-9])|(25[0-5]))\\.){3}(1?[1-9]?[0-9])|(10[0-9])|(2[0-4][0-9])|(25[0-5])";
    public IPAddress(String ipAddress) throws IPAddressFormatException {
        String[] tmp = ipAddress.split(":");
        if(tmp.length!=2){
            throw new IPAddressFormatException("Doesn't match IPv4 format. Does it have port number?");
        }
        if(!isIPAddress(tmp[0])){
            throw new IPAddressFormatException("Doesn't match IPv4 format.");
        }
        try{
            port = Integer.parseInt(tmp[1]);
        } catch(NumberFormatException e){
            throw new IPAddressFormatException("Port number must be a \"number\".");
        }
        this.ipAddress = tmp[0];
    }

    private boolean isIPAddress(String ipAddress){
        Pattern pattern_ip4 = Pattern.compile("^"+IPV4_FORMAT+"$");
        return checkPattern(pattern_ip4, ipAddress);
    }

    private boolean checkPattern(Pattern p, String target){
        Matcher m = p.matcher(target);
        return m.find();
    }

    public String getIPAddress(){
        return ipAddress;
    }

    public int getPort(){
        return port;
    }
    public class IPAddressFormatException extends Exception{

        private static final long serialVersionUID = -8987472593202932823L;
        public IPAddressFormatException(String message) {
            super(message);
        }
    }

    public static void main(String[] args){
        BufferedReader r =
                new BufferedReader(new InputStreamReader(System.in), 1);
        while(true){
            String line;
            try {
                line = r.readLine();
            } catch (IOException e1) {
                continue;
            }
            if(line.equals("exit")){
                break;
            }
            try {
                IPAddress ip = new IPAddress(line);
                System.out.println("IP Address:"+ip.getIPAddress());
                System.out.println("Port Number:"+ip.getPort());
            } catch (IPAddressFormatException e) {
                e.printStackTrace();
            }
        }
    }
}
