import java.io.*;
import java.util.Arrays;

public class Program {
    public static String[] fakeMain(String[] args) throws IOException, DatabaseException{
		
	String [] rezultat = new String[1000]; // rezultat, ki ga vrne funkcija main
        
        String potDoTxtDatoteke = "/home/tajci/podatki.txt";
        String potDoDatDatoteke = "/home/tajci/podatki.dat";
        
        // najprej podamo imena in dolzine polj v bazi
        FieldInfo[] fieldInfoObjectArray8 = new FieldInfo[9]; // tabela FieldInfo objektov, ki vsebujejo ime in dolz
        fieldInfoObjectArray8[0] = new FieldInfo("flightNumber", 5);
        fieldInfoObjectArray8[1] = new FieldInfo("originAirport", 3);
        fieldInfoObjectArray8[2] = new FieldInfo("destinationAirport", 3);
        fieldInfoObjectArray8[3] = new FieldInfo("carrier", 20);
        fieldInfoObjectArray8[4] = new FieldInfo("price", 7);
        fieldInfoObjectArray8[5] = new FieldInfo("day", 3);
        fieldInfoObjectArray8[6] = new FieldInfo("time", 5);
        fieldInfoObjectArray8[7] = new FieldInfo("duration", 6);
        fieldInfoObjectArray8[8] = new FieldInfo("availableSeats", 3);            

        Data x;

        File f = new File(potDoDatDatoteke);
        if(f.exists() && !f.isDirectory()) { 
            // do something
            System.out.println("Datoteka ze obstaja");
            // ce baza ze obstaja
            x = new Data(potDoDatDatoteke);
        }
        else { // sicer ustvarimo bazo

            x = new Data(potDoDatDatoteke, fieldInfoObjectArray8);
            String[] tmp = new String[9]; // za vnos vrednosti v .dat datoteko

        // beremo in txt datoteke

		try {
			File datoteka = new File(potDoTxtDatoteke);
			BufferedReader vhod = new BufferedReader(new InputStreamReader(new FileInputStream(datoteka), "UTF-8"));
			
			String niz;
			String[] vrsticaIzDatoteke;
			
			while ((niz = vhod.readLine()) != null) {
				vrsticaIzDatoteke = niz.trim().split("\\^");
                tmp[0] = vrsticaIzDatoteke[0];
                tmp[1] = vrsticaIzDatoteke[1];
                tmp[2] = vrsticaIzDatoteke[2];
                tmp[3] = vrsticaIzDatoteke[3];
                tmp[4] = vrsticaIzDatoteke[4];
                tmp[5] = vrsticaIzDatoteke[5];
                tmp[6] = vrsticaIzDatoteke[6];
                tmp[7] = vrsticaIzDatoteke[7];
                tmp[8] = vrsticaIzDatoteke[8];  

                x.add(tmp); // zapisemo v bazo, t.j. .dat datoteko
			}
			
			vhod.close(); // zapremo branje datoteke			
		}
		catch (Exception e) {
			System.out.println("Exception -> " + e.getMessage());
		} 
        }

        // modifikacija zapisa *********************************************************************************************************

        String[] tmp2 = new String[9];
        tmp2[0] = "SA002";
        tmp2[1] = "SFO";
        tmp2[2] = "LHR"; // London Heatrow
        tmp2[3] = "SpeedyAir";
        tmp2[4] = "2000";
        tmp2[5] = "Mon";
        tmp2[6] = "11:20";
        tmp2[7] = "11h65m";
        tmp2[8] = "21";

        //FieldInfo[] fieldInfoObjectArray2 = new FieldInfo[4]; // polje FieldInfo objektov, ki vsebujejo ime in dolz
        //fieldInfoObjectArray2[0] = new FieldInfo("flightNumber", 5); 
        //fieldInfoObjectArray2[1] = new FieldInfo("originAirport", 3);
        //fieldInfoObjectArray2[2] = new FieldInfo("destinationAirport", 3);
        //fieldInfoObjectArray2[3] = new FieldInfo("availableSeats", 3);      

        // ustvarimo nov objekt, ki vsebuje modificirane podatke, na ID poziciji "SA002"

        DataInfo fff = new DataInfo(2, fieldInfoObjectArray8, tmp2);
        // fff.values = tmp2;
        // x.modify(x.getRecord(1));
        // zapisemo zadevo v bazo
        x.modify(fff);

        // System.out.println("----------------------");
        // System.out.println(x.getRecord(1).getValues()[0]);
        // System.out.println(Arrays.toString(x.getRecord(1).getValues()));
        // System.out.println("----------------------");
        // System.out.println("----------------------");
        // System.out.println(x.getRecord(2).getValues()[0]);
        // System.out.println(Arrays.toString(x.getRecord(2).getValues()));
        // System.out.println("----------------------");

        DataInfo [] ggg = new DataInfo[999];
		
		String arg1 = args[0];
        String arg2 = args[1];
        String arg3 = "originAirport='" + arg1 + ",destinationAirport='" + arg2 + "'";

        ggg = x.criteriaFind(arg3);
 
        for(int i = 0; i<ggg.length; i++){
            System.out.println(Arrays.toString(ggg[i].getValues()));
			rezultat[i] = Arrays.toString(ggg[i].getValues());
        }

        //System.out.println("KONEC");
		return rezultat;
    }
}
