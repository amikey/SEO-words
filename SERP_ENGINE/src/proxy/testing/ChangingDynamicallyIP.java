package proxy.testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ChangingDynamicallyIP {
	
	private static String[] ips = {
        // eth0
		"178.33.123.238",
        // eth0:1
		"5.39.42.20",
        // eth0:2		
		"5.39.42.21",
        // eth0:3
		"5.39.42.22",
        // eth0:4
		"5.39.42.23"
	};

	private static String change_command = "sudo ip addr add 5.39.42.20 dev eth0";
	private static String show_command = "sudo ip addr show";
	private static String show2_command = "sudo ifconfig";
	private static String routeshow_command = "sudo ip route show";
	private static String enable_command = "ip link set eth0:1 up";
	private static String disable_command = "ip link set eth0:1 down";
	// knowing which ip is used for outbound trafic
	private static String which_address_for_outbound = "curl ifconfig.me";
	
	
	
	public static void main(String[] args){
		try {
			Process p = Runtime.getRuntime().exec(show2_command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line=reader.readLine();

			while (line != null) {    
				System.out.println(line);
				line = reader.readLine();
			}

		}
		catch(IOException e1) {}
		catch(InterruptedException e2) {}
		try {
			Process p = Runtime.getRuntime().exec(change_command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line=reader.readLine();

			while (line != null) {    
				System.out.println(line);
				line = reader.readLine();
			}

		}
		catch(IOException e1) {}
		catch(InterruptedException e2) {}
		try {
			Process p = Runtime.getRuntime().exec(show2_command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line=reader.readLine();

			while (line != null) {    
				System.out.println(line);
				line = reader.readLine();
			}

		}
		catch(IOException e1) {}
		catch(InterruptedException e2) {}
	}
}
