import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Cliente implements Runnable{
	
	private Socket cliente;
	private BufferedReader lector;
	private PrintWriter escritor;
	private boolean terminado;
	
	public Cliente() {
		this.terminado = false;
	}
	
	public void apagar() {
		terminado = true;
		try {
			if (lector != null) 
				lector.close();
			
			if (escritor != null)
				escritor.close();
			
			if (!cliente.isClosed()) 
				cliente.close();
		} catch(IOException e) {
			
		}
	}

	@Override
	public void run() {
		try {
			InetAddress ia = InetAddress.getLocalHost();
			cliente = new Socket(ia.getHostAddress(), 9999);
			escritor = new PrintWriter(cliente.getOutputStream(), true);
			lector = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
			
			AdministradorEntradaDatos aed = new AdministradorEntradaDatos();
			Thread t = new Thread(aed);
			t.start();
			
			String mensaje;
			while ((mensaje = lector.readLine()) != null) {
				System.out.println(mensaje);
			}
		} catch(IOException e) {
			apagar();
		}
	}
	
	class AdministradorEntradaDatos implements Runnable {

		@Override
		public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
				
				while (!terminado) {
					String mensaje = in.readLine();
					if (mensaje.equals("/quit")) {
						escritor.println(mensaje);
						in.close();
						apagar();
					} else {
						escritor.println(mensaje);
					}
				}
			} catch(IOException e) {
				apagar();
			}
		}
	}
	
	public static void main(String[] args) {
		Cliente c = new Cliente();
		c.run();
	}

}
