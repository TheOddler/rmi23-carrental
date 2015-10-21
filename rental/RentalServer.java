package rental;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import remote.IAgency;
import remote.ICarRentalCompany;
import remote.IManagerSession;
import shared.CarType;
import shared.ReservationException;

public class RentalServer {

	public static void main(String[] args) throws ReservationException,
			NumberFormatException, IOException, NotBoundException {
		// Disable security manager
		System.setSecurityManager(null);
		
		// Create and register companies
		System.out.println("Creation and registering companies with name service...");
		Registry registry = LocateRegistry.getRegistry();
		
		// To simulate the difference between registries I add the company under a different name to the registry
		// This makes sure (hopefully) that it's not possible to accidentally get the company directly from
		//the registry, rather than going through the agency. Normally the companies would be on a different
		//naming server, but for testing they are all local, which only has a single naming server.
		
		String hertzName = "Hertz";
		String hertzNameInRegistry = "Company: Hertz";
		ICarRentalCompany hertz = createCompanyRegister(hertzNameInRegistry, hertzName, "hertz.csv", registry);
		
		String dockxName = "Dockx";
		String dockxNameInRegistry = "Company: Dockx";
		ICarRentalCompany dockx = createCompanyRegister(dockxNameInRegistry, dockxName, "dockx.csv", registry);
		
		
		// Get agency and register companies there
		System.out.println("Looking for agency...");
		Registry agencyRegistry = LocateRegistry.getRegistry(); // for testing it's also the local one, but could be a remote one
		IAgency agency = (IAgency)agencyRegistry.lookup(IAgency.DEFAULT_REMOTE_AGENCY_NAME);
		// Only managers can register companies, so we make a manager session
		System.out.println("Starting manager session...");
		IManagerSession ses = agency.startManagerSession("Hertz and Docks Manager Joe");
		System.out.println(ses.hello());
		System.out.println("Registering companies with agency...");
		ses.registerCarRentalCompany(hertz);
		ses.registerCarRentalCompany(dockx);
		agency.endManagerSession(ses);
		System.out.println("All done.");
	}
	
	private static ICarRentalCompany createCompanyRegister(String bindName, String compName, String dataFileName, Registry registry) throws NumberFormatException, ReservationException, IOException {
		List<Car> cars = loadData(dataFileName);
		CarRentalCompany crc = new CarRentalCompany(compName, cars); 
		ICarRentalCompany stub = (ICarRentalCompany) UnicastRemoteObject.exportObject(crc, 0);
		registry.rebind(bindName, stub); //use rebind in case something with the same name already exists
		
		return stub;
	}

	public static List<Car> loadData(String dataFileName)
			throws ReservationException, NumberFormatException, IOException {

		List<Car> cars = new LinkedList<Car>();

		int nextuid = 0;

		// open file
		BufferedReader in = new BufferedReader(new FileReader(dataFileName));

		try {
			// while next line exists
			while (in.ready()) {
				// read line
				String line = in.readLine();
				// if comment: skip
				if (line.startsWith("#"))
					continue;
				// tokenize on ,
				StringTokenizer csvReader = new StringTokenizer(line, ",");
				// create new car type from first 5 fields
				CarType type = new CarType(csvReader.nextToken(),
						Integer.parseInt(csvReader.nextToken()),
						Float.parseFloat(csvReader.nextToken()),
						Double.parseDouble(csvReader.nextToken()),
						Boolean.parseBoolean(csvReader.nextToken()));
				System.out.println(type);
				// create N new cars with given type, where N is the 5th field
				for (int i = Integer.parseInt(csvReader.nextToken()); i > 0; i--) {
					cars.add(new Car(nextuid++, type));
				}
			}
		} finally {
			in.close();
		}

		return cars;
	}
}
