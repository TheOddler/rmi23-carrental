package agency;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import remote.IAgency;
import remote.ICarRentalCompany;
import remote.IManagerSession;
import remote.IReservationSession;
import remote.ISession;

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
	
	private Map<String, ICarRentalCompany> companies = new HashMap<>();
	private Set<ISession> sessions = new HashSet<>();
	
	public Agency() {
		
	}

	@Override
	public String hello() {
		return "Ahoy!, I'm t' agency.";
	}

	@Override
	public IReservationSession startReservationSession(String client) throws RemoteException {
		IReservationSession ses = new ReservationSession(this, client);
		sessions.add(ses);
		return (IReservationSession) UnicastRemoteObject.exportObject(ses, 0);
	}

	@Override
	public IManagerSession startManagerSession() throws RemoteException {
		IManagerSession ses = new ManagerSession(this);
		sessions.add(ses);
		return (IManagerSession) UnicastRemoteObject.exportObject(ses, 0);
	}

	@Override
	public void endSession(ISession ses) {
		sessions.remove(ses);
	}
	
	ICarRentalCompany getCompany(String name) {
		return companies.get(name);
	}
	
	void registerCompany(Registry registry, String name) throws RemoteException, NotBoundException {
		System.out.println("Registering company: '" + name + "' from this registry: " + registry);
		
		ICarRentalCompany comp = (ICarRentalCompany) registry.lookup(name);
		companies.put(comp.getName(), comp);
	}
	
	void unregisterCarRentalCompany(String name) {
		companies.remove(name);
	}
	
	Collection<String> getCarRentalCompanyNames() {
		return companies.keySet();
	}
	
	Collection<ICarRentalCompany> getCarRentalCompanies() {
		return companies.values();
	}
}
