package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ISession extends Remote {
	
	String hello() throws RemoteException; //for testing and politeness
	
	// Maybe TODO: Sessions are now "ended" by telling the agency.
	// However they can still be used, they are just removed from the list of session in the agency.
	// So maybe make the session remember it is ended, and don't allow any more interaction.
	// Alternatively, just remove the "end" method from the agency, and don't keep sessions in a list.
}
