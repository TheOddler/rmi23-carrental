package agency;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import remote.ICarRentalCompany;
import remote.IReservationSession;
import shared.CarType;
import shared.Quote;
import shared.Reservation;
import shared.ReservationConstraints;
import shared.ReservationException;

public class ReservationSession implements IReservationSession {
	
	Agency agency;
	String client;
	Set<Quote> quotes = new HashSet<>();
	
	public ReservationSession(Agency agency, String client) {
		this.agency = agency;
		this.client = client;
	}

	@Override
	public String hello() throws RemoteException {
		return "Hi! I like reserving things.";
	}

	@Override
	public Quote createQuote(ReservationConstraints constraints, String company) throws RemoteException, ReservationException {
		ICarRentalCompany comp = agency.getCompany(company);
		Quote quote = comp.createQuote(constraints, client);
		
		quotes.add(quote);
		return quote;
	}

	@Override
	public Collection<Quote> getCurrentQuotes() {
		return quotes;
	}

	@Override
	public Collection<Reservation> confirmQuotes() throws RemoteException, ReservationException {
		List<Reservation> reservations = new ArrayList<>();
		try {
			for(Quote quote: quotes) {
				ICarRentalCompany comp = agency.getCompany(quote.getRentalCompany());
				Reservation res = comp.confirmQuote(quote);
				reservations.add(res);
			}
		}
		catch (ReservationException e) {
			for(Reservation res: reservations) {
				ICarRentalCompany comp = agency.getCompany(res.getRentalCompany());
				comp.cancelReservation(res);
			}
			throw e;
		}
		catch (RemoteException e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			quotes.clear();
		}
		
		return reservations;
	}

	@Override
	public Collection<CarType> getAvailableCarTypes(Date from, Date to) throws RemoteException {
		Set<CarType> types = new HashSet<>();
		for(ICarRentalCompany comp: agency.getCarRentalCompanies()) {
			types.addAll(comp.getAvailableCarTypes(from, to));
		}
		return types;
	}

	@Override
	public CarType getCheapestCarType(Date from, Date to) throws RemoteException {
		Collection<CarType> types = getAvailableCarTypes(from, to);
		
		double minCost = Double.MAX_VALUE;
		CarType cheapest = null;
		for(CarType type: types) {
			double cost = type.getRentalPricePerDay();
			if (cost < minCost) {
				minCost = cost;
				cheapest = type;
			}
		}
		
		return cheapest;
	}
	
}
