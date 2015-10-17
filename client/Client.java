package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import remote.IAgency;
import remote.IManagerSession;
import remote.IReservationSession;
import shared.CarType;
import shared.Reservation;
import shared.ReservationConstraints;

public class Client extends AbstractTestManagement<IReservationSession, IManagerSession> {
	
	/********
	 * MAIN *
	 ********/
	
	public static void main(String[] args) throws Exception {
		System.setSecurityManager(null);
		
		Registry registry = LocateRegistry.getRegistry(); // For testing use localhost, but could be remote
		IAgency agency = (IAgency) registry.lookup(IAgency.DEFAULT_REMOTE_AGENCY_NAME);
		
		System.out.println(agency.hello());
		
		// An example reservation scenario on car rental company 'Hertz' would be...
		Client client = new Client("trips", agency);
		client.run();
	}
	
	/********
	 * Vars *
	 ********/
	private IAgency agency;
	
	/***************
	 * CONSTRUCTOR 
	 * @throws RemoteException 
	 * @throws NotBoundException
	 ***************/
	
	public Client(String scriptFile, IAgency agency) throws RemoteException, NotBoundException {
		super(scriptFile);
		this.agency = agency;
	}

	@Override
	protected String getCheapestCarType(IReservationSession session,
			Date start, Date end) throws Exception {
		return session.getCheapestCarType(start, end).getName();
	}

	@Override
	protected String getMostPopularCarRentalCompany(IManagerSession ms)
			throws Exception {
		return ms.getMostPopularCarRentalCompany();
	}

	@Override
	protected IReservationSession getNewReservationSession(String name)
			throws Exception {
		return agency.startReservationSession(name);
	}

	@Override
	protected IManagerSession getNewManagerSession(String name,
			String carRentalName) throws Exception {
		return agency.startManagerSession();
	}

	@Override
	protected void checkForAvailableCarTypes(IReservationSession session,
			Date start, Date end) throws Exception {
		Collection<CarType> available = session.getAvailableCarTypes(start, end);
		System.out.println("Available car types: ");
		for (CarType type: available) {
			System.out.println(type);	
		}
	}

	@Override
	protected void addQuoteToSession(IReservationSession session, String name, Date start, Date end, String carType, String carRentalName)
			throws Exception {
		ReservationConstraints constraints = new ReservationConstraints(start, end, carType);
		session.createQuote(constraints, carRentalName);
	}

	@Override
	protected List<Reservation> confirmQuotes(IReservationSession session, String name) throws Exception {
		return new ArrayList<Reservation>(session.confirmQuotes());
	}

	@Override
	protected int getNumberOfReservationsBy(IManagerSession ms, String clientName) throws Exception {
		return ms.getNumberOfReservationsBy(clientName);
	}

	@Override
	protected int getNumberOfReservationsForCarType(IManagerSession ms, String carRentalName, String carType) throws Exception {
		return ms.getNumberOfReservationsForTypeByName(carType, carRentalName);
	}
}