package remote;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.rmi.Remote;
import java.rmi.RemoteException;

import shared.CarType;
import shared.Quote;
import shared.Reservation;
import shared.ReservationConstraints;
import shared.ReservationException;

public interface ICarRentalCompany extends Remote {

	/********
	 * NAME *
	 ********/

	String getName()
		throws RemoteException;

	/*************
	 * CAR TYPES *
	 *************/

	Collection<CarType> getAllCarTypes()
		throws RemoteException;

	CarType getCarType(String carTypeName)
		throws RemoteException;

	boolean isAvailable(String carTypeName, Date start, Date end)
		throws RemoteException;

	Set<CarType> getAvailableCarTypes(Date start, Date end)
		throws RemoteException;

	/****************
	 * RESERVATIONS *
	 ****************/

	Quote createQuote(ReservationConstraints constraints, String client)
		throws ReservationException, RemoteException;

	Reservation confirmQuote(Quote quote)
		throws ReservationException, RemoteException;

	void cancelReservation(Reservation res)
		throws RemoteException;
	
	List<Reservation> getReservationsByRenter(String clientName)
		throws RemoteException;

	int getNumberOfReservationsForTypeByName(String type)
		throws RemoteException;

	int getNumberOfReservationsBy(String renter)
		throws RemoteException;
	
	int getTotalNumberOfReservations()
			throws RemoteException;
	
	/**
	 * Test
	 */
	String hello() throws RemoteException;

}