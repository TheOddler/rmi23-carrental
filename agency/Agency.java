package agency;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import remote.IAgency;
import remote.ICarRentalCompany;
import remote.IManagerSession;
import remote.IReservationSession;

public class Agency implements IAgency {
	
	public static void main(String[] args) throws RemoteException {
		// Disable security manager
		System.setSecurityManager(null);
		
		// Create agency & stub
		System.out.println("Creating agency...");
		Agency agency = new Agency();
		IAgency stub = (IAgency) UnicastRemoteObject.exportObject(agency, 0);
		
		// First find the RMI register, then register our object.
		System.out.println("Registering agency...");
		Registry registry = LocateRegistry.getRegistry();
		registry.rebind(IAgency.DEFAULT_REMOTE_AGENCY_NAME, stub); //use rebind in case something with the same name already exists
		
		System.out.println("Done.");
	}
	
	private Map<String, IReservationSession> resSessions = new HashMap<>();
	private Map<String, IManagerSession> manSessions = new HashMap<>();

	private Map<String, ICarRentalCompany> companies = new HashMap<>();
	
	public Agency() {
		
	}

	@Override
	public String hello() {
		return "Ahoy!, I'm t' agency.";
	}

	// I simply use a string for identifying clients and managers here
	// I assume they are always unique and different client have different names
	// I also assume the same client doesn't try to log in from multiple devices
	// That's why these methods don't need to be synchronized
	@Override
	public IReservationSession startReservationSession(String client) throws RemoteException {
		if (!resSessions.containsKey(client))
		{
			IReservationSession ses = new ReservationSession(this, client);
			IReservationSession remote = (IReservationSession) UnicastRemoteObject.exportObject(ses, 0);
			resSessions.put(client, remote);
		}
		return resSessions.get(client);
	}

	@Override
	public IManagerSession startManagerSession(String name) throws RemoteException {
		if (!manSessions.containsKey(name)) {
			IManagerSession ses = new ManagerSession(this);
			IManagerSession remote = (IManagerSession) UnicastRemoteObject.exportObject(ses, 0);
			manSessions.put(name, remote);
		}
		return manSessions.get(name);
	}
	
	@Override
	public void endReservationSession(IReservationSession ses) {
		resSessions.remove(ses);
	}

	@Override
	public void endManagerSession(IManagerSession ses) {
		manSessions.remove(ses);
	}
	
	
	
	synchronized ICarRentalCompany getCompany(String name) {
		return companies.get(name);
	}
	
	synchronized void registerCompany(ICarRentalCompany comp) throws RemoteException, NotBoundException {
		companies.put(comp.getName(), comp);
	}
	
	synchronized void unregisterCarRentalCompany(String name) {
		companies.remove(name);
	}
	
	synchronized Collection<String> getCarRentalCompanyNames() {
		return companies.keySet();
	}
	
	synchronized Collection<ICarRentalCompany> getCarRentalCompanies() {
		return companies.values();
	}
}
