package agency;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;

import remote.ICarRentalCompany;
import remote.IManagerSession;
import shared.CarType;

public class ManagerSession implements IManagerSession {
	
	Agency agency;
	
	ManagerSession(Agency agency) {
		this.agency = agency;
	}

	@Override
	public String hello() throws RemoteException {
		return "Hello, we can do manager-stuff together!";
	}

	@Override
	public void registerCarRentalCompany(ICarRentalCompany comp) throws RemoteException, NotBoundException {
		agency.registerCompany(comp);
	}

	@Override
	public void unregisterCarRentalCompany(String name) throws RemoteException {
		agency.unregisterCarRentalCompany(name);
	}

	@Override
	public Collection<String> getCarRentalCompanies() {
		return agency.getCarRentalCompanyNames();
	}

	@Override
	public Collection<CarType> getCarTypesOf(String company) throws RemoteException {
		return agency.getCompany(company).getAllCarTypes();
	}

	@Override
	public int getNumberOfReservationsForTypeByName(String type, String company) throws RemoteException {
		return agency.getCompany(company).getNumberOfReservationsForTypeByName(type);
	}

	@Override
	public int getNumberOfReservationsBy(String renter) throws RemoteException {
		int count = 0;
		for (ICarRentalCompany comp: agency.getCarRentalCompanies()) {
			count += comp.getNumberOfReservationsBy(renter);
		}
		return count;
	}

	@Override
	public String getMostPopularCarRentalCompany() throws RemoteException {
		int curMax = 0;
		ICarRentalCompany curBest = null;
		
		for(ICarRentalCompany comp: agency.getCarRentalCompanies()) {
			int curCount = comp.getTotalNumberOfReservations();
			if (curCount > curMax) {
				curMax = curCount;
				curBest = comp;
			}
		}
		
		return curBest.getName();
	}
	
}
