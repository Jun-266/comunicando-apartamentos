import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Servidor implements Runnable {

	private ArrayList<AdministradorConexiones> conexiones;
	private ServerSocket servidor;
	private boolean terminado;
	private ExecutorService piscina;

	public Servidor() {
		this.conexiones = new ArrayList<>();
		this.terminado = false;
	}

	@Override
	public void run() {
		try {
			servidor = new ServerSocket(9999);
			piscina = Executors.newCachedThreadPool();
			while (!terminado) {
				Socket cliente = servidor.accept();
				AdministradorConexiones conexion = new AdministradorConexiones(cliente);
				conexiones.add(conexion);
				piscina.execute(conexion);
			}
		} catch (IOException e) {
			apagar();
		}
	}

	public void transmitir(String mensaje) {
		for (AdministradorConexiones ac : conexiones) {
			if (ac != null) {
				ac.enviarMensaje(mensaje);
			}
		}
	}

	public void apagar() {
		terminado = true;
		piscina.shutdown();
		try {

			if (!servidor.isClosed()) 
				servidor.close();
			
			for (AdministradorConexiones ac : conexiones) {
				ac.apagar();
			}
		} catch(IOException e) {
			
		}
	}

	class AdministradorConexiones implements Runnable {

		private Socket cliente;
		private BufferedReader lector;
		private PrintWriter escritor;
		private String nombre;

		public AdministradorConexiones(Socket cliente) {
			this.cliente = cliente;
		}

		public void enviarMensaje(String mensaje) {
			escritor.println(mensaje);
		}
		
		public void apagar() {
			try {
				if (cliente != null) 
					cliente.close();
				
				if (lector != null) 
					lector.close();
				
				if (escritor != null) 
					escritor.close();
			} catch(IOException e) {
				
			}
		}

		@Override
		public void run() {
			try {
				escritor = new PrintWriter(cliente.getOutputStream(), true);
				lector = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
				escritor.println("Por favor inserta tu nombre: ");
				nombre = lector.readLine();
				System.out.println(nombre + " se ha conectado!");
				transmitir(nombre + " se unió al chat!");
				String mensaje;

				while ((mensaje = lector.readLine()) != null) {
					if (mensaje.startsWith("/nick ")) {
						String[] mensajeDividido = mensaje.split(" ", 2);
						if (mensajeDividido.length == 2) {
							transmitir(nombre + " renombrado a " + mensajeDividido[1]);
							System.out.println(nombre + " renombrado a " + mensajeDividido[1]);
							nombre = mensajeDividido[1];
							escritor.println("Nombre modificado con exito a: " + nombre);
						} else {
							escritor.println("No se registro ningun nombre.");
						}
					} else if (mensaje.startsWith("/quit")) {
						transmitir(nombre + " dejo el chat.");
						apagar();
					} else {
						transmitir(nombre + ": " + mensaje);
					}
				}
			} catch (IOException e) {
				apagar();
			}
		}
	}
	
	public static void main(String[] args) {
		Servidor servidor = new Servidor();
		servidor.run();
	}

}
