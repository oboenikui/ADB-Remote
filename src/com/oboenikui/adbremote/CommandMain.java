package com.oboenikui.adbremote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.oboenikui.adbremote.IPAddress.IPAddressFormatException;

public class CommandMain {
    public static void main(String[] args){
        List<Integer> ports = new ArrayList<Integer>();
        List<String> ips = new ArrayList<String>();
        List<String> devices = new ArrayList<String>();

        try {
            Process p = Runtime.getRuntime().exec("adb devices");
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s;
            while((s=br.readLine()).startsWith("*")){
                System.out.println(s);
            }
            System.out.println(s);
            while((s = br.readLine())!=null){
                if(s.trim().isEmpty()|| s.startsWith("*")){
                    break;
                }
                System.out.println(s);
                String[] tmp = s.split("\t");
                String deviceID = tmp[0];
                String state = tmp[1];
                if(deviceID.contains("emulator")||!state.equals("device")){
                    continue;
                }
                try {
                    IPAddress ip = new IPAddress(deviceID);
                    ports.add(ip.getPort());
                    ips.add(ip.getIPAddress());
                } catch (IPAddressFormatException e) {
                    devices.add(deviceID);
                }
            }
            for(String device:devices){
                p = Runtime.getRuntime().exec("adb -s "+device+" shell ls /sys/class/net");
                br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String ip=null;
                int port=-1;
                while((s=br.readLine())!=null){
                    if(s.equals("wlan0")){
                        p = Runtime.getRuntime().exec("adb -s "+device+" shell ifconfig wlan0");
                        ip = getIPAddress(new BufferedReader(new InputStreamReader(p.getInputStream())).readLine());
                        if(ips.contains(ip)){
                            break;
                        }
                        port = findNextPort(ports);
                        Runtime.getRuntime().exec("adb -s "+device+" tcpip "+port);
                        p = Runtime.getRuntime().exec("adb -s "+device+" connect "+ip+":"+port);
                        if(!new BufferedReader(new InputStreamReader(p.getInputStream())).readLine().contains("unable")){
                            ports.add(port);
                            ips.add(ip);
                            break;
                        }
                    }
                    if(s.equals("eth0")){
                        p = Runtime.getRuntime().exec("adb -s "+device+" shell ifconfig eth0");
                        ip = getIPAddress(new BufferedReader(new InputStreamReader(p.getInputStream())).readLine());
                        if(ips.contains(ip)){
                            break;
                        }
                        port = findNextPort(ports);
                        Runtime.getRuntime().exec("adb -s "+device+" tcpip "+port);
                        p = Runtime.getRuntime().exec("adb -s "+device+" connect "+ip+":"+port);
                        if(!new BufferedReader(new InputStreamReader(p.getInputStream())).readLine().contains("unable")){
                            ports.add(port);
                            ips.add(ip);
                            break;
                        }
                    }
                }
                if(ip!=null&&port!=-1){
                    System.out.println("Connected to "+device+". ("+ip+":"+port+")");
                } else if(ip!=null){
                    System.out.println(device+" has already connected.");
                } else {
                    System.err.println("Can't connect to "+device+".");
                }
            }
        } catch (IOException e1) {
            System.err.println(e1.getLocalizedMessage());
        }
    }

    public static String getIPAddress(String origin){
        Pattern pattern = Pattern.compile(IPAddress.IPV4_FORMAT);
        Matcher matcher = pattern.matcher(origin);
        if(matcher.find()){
            return matcher.group();
        }
        return null;
    }

    public static int findNextPort(List<Integer> ports){
        int i = 5554;
        for(int port:ports){
            if(i<port){
                i=port;
            }
        }
        return i+1;
    }
}
