package remote;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;

import shared.CarType;
import shared.Quote;
import shared.Reservation;
import shared.ReservationConstraints;
import shared.ReservationException;

public interface IReservationSession extends ISession {
	
	Quote createQuote(ReservationConstraints constraints, String company) throws RemoteException, ReservationException;
	Collection<Quote> getCurrentQuotes() throws RemoteException;
	Collection<Reservation> confirmQuotes() throws RemoteException, ReservationException;
	
	Collection<CarType> getAvailableCarTypes(Date from, Date to) throws RemoteException;
	CarType getCheapestCarType(Date from, Date to) throws RemoteException;
}
