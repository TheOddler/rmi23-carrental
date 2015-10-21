package rental;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import remote.ICarRentalCompany;
import shared.CarType;
import shared.Quote;
import shared.Reservation;
import shared.ReservationConstraints;
import shared.ReservationException;

public class CarRentalCompany implements ICarRentalCompany {

	private static Logger logger = Logger.getLogger(CarRentalCompany.class.getName());
	
	private String name;
	private List<Car> cars;
	private Map<String,CarType> carTypes = new HashMap<String, CarType>();

	/***************
	 * CONSTRUCTOR *
	 ***************/

	public CarRentalCompany(String name, List<Car> cars) {
		logger.log(Level.INFO, "<{0}> Car Rental Company {0} starting up...", name);
		this.name = name;
		this.cars = cars;
		for(Car car:cars)
			carTypes.put(car.getType().getName(), car.getType());
	}

	/* (non-Javadoc)
	 * @see rental.ICarRentalompany#getName()
	 */

	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see rental.ICarRentalompany#getAllCarTypes()
	 */

	@Override
	public Collection<CarType> getAllCarTypes() {
		return carTypes.values();
	}
	
	/* (non-Javadoc)
	 * @see rental.ICarRentalompany#getCarType(java.lang.String)
	 */
	@Override
	public CarType getCarType(String carTypeName) {
		if(carTypes.containsKey(carTypeName))
			return carTypes.get(carTypeName);
		throw new IllegalArgumentException("<" + carTypeName + "> No car type of name " + carTypeName);
	}
	
	/* (non-Javadoc)
	 * @see rental.ICarRentalompany#isAvailable(java.lang.String, java.util.Date, java.util.Date)
	 */
	@Override
	public boolean isAvailable(String carTypeName, Date start, Date end) {
		logger.log(Level.INFO, "<{0}> Checking availability for car type {1}", new Object[]{name, carTypeName});
		if(carTypes.containsKey(carTypeName))
			return getAvailableCarTypes(start, end).contains(carTypes.get(carTypeName));
		throw new IllegalArgumentException("<" + carTypeName + "> No car type of name " + carTypeName);
	}
	
	/* (non-Javadoc)
	 * @see rental.ICarRentalompany#getAvailableCarTypes(java.util.Date, java.util.Date)
	 */
	@Override
	public Set<CarType> getAvailableCarTypes(Date start, Date end) {
		Set<CarType> availableCarTypes = new HashSet<CarType>();
		for (Car car : cars) {
			if (car.isAvailable(start, end)) {
				availableCarTypes.add(car.getType());
			}
		}
		return availableCarTypes;
	}
	
	/*********
	 * CARS *
	 *********/
	
	private Car getCar(int uid) {
		for (Car car : cars) {
			if (car.getId() == uid)
				return car;
		}
		throw new IllegalArgumentException("<" + name + "> No car with uid " + uid);
	}
	
	private List<Car> getAvailableCars(String carType, Date start, Date end) {
		List<Car> availableCars = new LinkedList<Car>();
		for (Car car : cars) {
			if (car.getType().getName().equals(carType) && car.isAvailable(start, end)) {
				availableCars.add(car);
			}
		}
		return availableCars;
	}

	/* (non-Javadoc)
	 * @see rental.ICarRentalompany#createQuote(rental.ReservationConstraints, java.lang.String)
	 */

	@Override
	synchronized public Quote createQuote(ReservationConstraints constraints, String client)
			throws ReservationException {
		logger.log(Level.INFO, "<{0}> Creating tentative reservation for {1} with constraints {2}", 
                        new Object[]{name, client, constraints.toString()});
		
		CarType type = getCarType(constraints.getCarType());
		
		if(!isAvailable(constraints.getCarType(), constraints.getStartDate(), constraints.getEndDate()))
			throw new ReservationException("<" + name
				+ "> No cars available to satisfy the given constraints.");
		
		double price = calculateRentalPrice(type.getRentalPricePerDay(),constraints.getStartDate(), constraints.getEndDate());
		
		return new Quote(client, constraints.getStartDate(), constraints.getEndDate(), getName(), constraints.getCarType(), price);
	}

	// Implementation can be subject to different pricing strategies
	private double calculateRentalPrice(double rentalPricePerDay, Date start, Date end) {
		return rentalPricePerDay * Math.ceil((end.getTime() - start.getTime())
						/ (1000 * 60 * 60 * 24D));
	}

	/* (non-Javadoc)
	 * @see rental.ICarRentalompany#confirmQuote(rental.Quote)
	 */
	@Override
	synchronized public Reservation confirmQuote(Quote quote) throws ReservationException {
		logger.log(Level.INFO, "<{0}> Reservation of {1}", new Object[]{name, quote.toString()});
		List<Car> availableCars = getAvailableCars(quote.getCarType(), quote.getStartDate(), quote.getEndDate());
		if(availableCars.isEmpty())
			throw new ReservationException("Reservation failed, all cars of type " + quote.getCarType()
	                + " are unavailable from " + quote.getStartDate() + " to " + quote.getEndDate());
		Car car = availableCars.get((int)(Math.random()*availableCars.size()));
		
		Reservation res = new Reservation(quote, car.getId());
		car.addReservation(res);
		return res;
	}

	/* (non-Javadoc)
	 * @see rental.ICarRentalompany#cancelReservation(rental.Reservation)
	 */
	@Override
	synchronized public void cancelReservation(Reservation res) {
		logger.log(Level.INFO, "<{0}> Cancelling reservation {1}", new Object[]{name, res.toString()});
		getCar(res.getCarId()).removeReservation(res);
	}

	@Override
	public String hello() throws RemoteException {
		return "Hi, I'm a car rental company. My name is " + getName();
	}

	@Override
	synchronized public List<Reservation> getReservationsByRenter(String clientName)
			throws RemoteException {
		List<Reservation> reservations = new ArrayList<Reservation>();
		
		for(Car car: cars) {
			for(Reservation res: car.getReservations()) {
				if (res.getCarRenter().equals(clientName)) {
					reservations.add(res);
				}
			}
		}
		
		return reservations;
	}

	@Override
	synchronized public int getNumberOfReservationsForTypeByName(String carType)
			throws RemoteException {
		int count = 0;
		
		for(Car car: cars) {
			if (car.getType().getName().equals(carType)) {
				count += car.getReservations().size();
			}
		}
		
		return count;
	}

	@Override
	synchronized public int getNumberOfReservationsBy(String renter) throws RemoteException {
		return getReservationsByRenter(renter).size();
	}

	@Override
	synchronized public int getTotalNumberOfReservations() throws RemoteException {
		int count = 0;
		
		for(Car car: cars) {
			count += car.getReservations().size();
		}
		
		return count;
	}
}