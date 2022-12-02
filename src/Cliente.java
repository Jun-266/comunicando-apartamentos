import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Cliente implements Runnable{
	
	private Socket cliente;
	private BufferedReader lector;
	private PrintWriter escritor;
	private boolean terminado;
	private String contacto;
	private String email;
	private String pass;
	private String name;
	
	public Cliente() {
		this.terminado = false;
		contacto="";
		email="";
		pass="";
		name="";
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
				System.out.println("Recuerda:\n1)Comunicarte con un apartamento\n2)Boton de emergencia\nSi quieres transmitir a todos los apartamentos"
						+" solo escribe");
				System.out.println("Digite un correo como contacto de emergencia");
				contacto=in.readLine();
				System.out.println("Digite su nombre");
				name = in.readLine();
				System.out.println("Digite su correo de gmail");
				email = in.readLine();
				System.out.println("Digite contraseña");
				pass=in.readLine();
				
				while (!terminado) {
					String mensaje = in.readLine();
					if (mensaje.equals("/quit")) {
						escritor.println(mensaje);
						in.close();
						apagar();
					}
					else if(mensaje.equals("2")) {
						escritor.println(mensaje);
						enviarCorreo();
					}
					else {
						escritor.println(mensaje);
					}
				}
			} catch(IOException e) {
				apagar();
			}
		}
		
		public void enviarCorreo() {
			Properties propiedad = new Properties();
	        propiedad.setProperty("mail.smtp.host", "smtp.gmail.com");
	        propiedad.setProperty("mail.smtp.starttls.enable", "true");
	        propiedad.setProperty("mail.smtp.port", "587");
	        propiedad.setProperty("mail.smtp.auth", "true");
	        

	        Session sesion = Session.getDefaultInstance(propiedad);
	        String asunto = "Emergencia apartamento";
	        String mensaje="El residente "+name+" se encuentra en un estado emergencia."
	        		+ "Por favor contactarse con la persona lo antes posible";
	        
	        MimeMessage correo = new MimeMessage(sesion);
	        
            try {
            	correo.setFrom(new InternetAddress (email));
                correo.addRecipient(Message.RecipientType.TO, new InternetAddress (contacto));
                correo.setSubject(asunto);
                correo.setText(mensaje);
                
                Transport transportar = sesion.getTransport("smtp");
                transportar.connect(email,pass);
                transportar.close();
				transportar.sendMessage(correo, correo.getRecipients(Message.RecipientType.TO));
			} catch ( AddressException e) {
				e.printStackTrace();
			}catch( MessagingException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		Cliente c = new Cliente();
		c.run();
	}

}
