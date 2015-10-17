package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IAgency extends Remote {
	
	public static final String DEFAULT_REMOTE_AGENCY_NAME = "agency";
	
	String hello() throws RemoteException;
	
	IReservationSession startReservationSession(String client)
			throws RemoteException;
	IManagerSession startManagerSession()
			throws RemoteException;
	
	void endSession(ISession ses)
			throws RemoteException;
	
}
