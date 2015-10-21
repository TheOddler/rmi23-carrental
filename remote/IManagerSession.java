package remote;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

import shared.CarType;

public interface IManagerSession extends Remote {

	String hello() throws RemoteException;
	
	void registerCarRentalCompany(ICarRentalCompany comp) throws RemoteException, NotBoundException;
	void unregisterCarRentalCompany(String name) throws RemoteException;
	
	Collection<String> getCarRentalCompanies() throws RemoteException;
	Collection<CarType> getCarTypesOf(String company) throws RemoteException;
	
	int getNumberOfReservationsForTypeByName(String type, String company) throws RemoteException;
	int getNumberOfReservationsBy(String renter) throws RemoteException;
	String getMostPopularCarRentalCompany() throws RemoteException;
}
