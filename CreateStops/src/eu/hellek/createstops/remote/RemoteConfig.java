package eu.hellek.createstops.remote;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class RemoteConfig {

	private static String code = "<change me>";
	
//	private static String url = "http://localhost:8888/rm/RemoteServlet";
	private static String url = "http://viaja-facil.appspot.com/rm/RemoteServlet";
	
//	public static String sampleText = getCode() + "a100,A - Estación Lanús por Pavón,bus,-34.584340690569164,-58.37536096572876,Estación de Omnibus Retiro,-34.585979,-58.372505,Av. Antártida Argentina,-34.588135,-58.37177699999999,Av. Dr. José María Ramos Mejía,-34.59287289176165,-58.37566137313843,Juncal,-34.592815,-58.38287,Cerrito,-34.608433511345886,-58.381969928741455,Lima,-34.620102,-58.38149599999999,Lima,-34.62466,-58.381454,Constitución,-34.624676,-58.382313,Lima Oeste,-34.62787577138091,-58.38227033615112,Av. Brasil,-34.627838,-58.381413,Lima,-34.63081555320525,-58.38139057159424,Av. Caseros,-34.632198,-58.383385,Dr. Ramón Carrillo,-34.633269705695085,-58.38338613510132,Dr. Ramón Carrillo,-34.63451440996254,-58.38243126869202,Dr. Ramón Carrillo,-34.640155069641146,-58.3808434009552,Dr. Ramón Carrillo,-34.642132299281435,-58.38090777397156,Av. Suárez,-34.642227,-58.379932,Vieytes,-34.646393,-58.378281,Alvarado,-34.646568,-58.379093,San Antonio,-34.6481254885834,-58.37856888771057,California,-34.64727817147093,-58.37532877922058,Herrera,-34.649727,-58.374588,Au. 9 de Julio,-34.65066738796234,-58.37312936782837,Au. 9 de Julio,-34.658009,-58.370338,Au. 9 de Julio,-34.65883977236554,-58.37156295776367,Av. Pavón,-34.659542,-58.37373,Av. Pavón,-34.661628436194704,-58.37536096572876,Av. Pavón,-34.668855580635466,-58.38031768798828,Av. Pavón,-34.674626254249276,-58.38225960731506,Helguera,-34.67572034274512,-58.38082194328308,Gútenberg,-34.67416743864327,-58.38032841682434,Av. Adolfo Alsina,-34.67159096534001,-58.37815046310425,Av. Crisólogo Larralde,-34.67224391478609,-58.375704288482666,Av. Crisólogo Larralde,-34.678834886502685,-58.36835503578186,Av. Gral. Güemes,-34.68133173075524,-58.37138056755066,Heredia,-34.68201989367483,-58.37054371833801,Av. Cnel. Lacarra,-34.68868948407182,-58.37857961654663,Sgto. Cabral,-34.68935112048953,-58.37785005569458,Rep. de Libano,-34.690266,-58.378986,Av. Sánchez de Bustamante,-34.69177707544292,-58.376981019973755,Gral. Donovan,-34.69513800826776,-58.38143348693848,Bolanos,-34.697696107128735,-58.378504514694214,Pres. Sarmiento,-34.70353533306816,-58.38600397109985,Pres. Sarmiento,-34.70632249199277,-58.38884711265564,Sitio de Montevideo,-34.70707218643714,-58.3878493309021,Anatole France,-34.71000915922493,-58.39113235473633,29 de Septiembre";
	
	public static String getUrl() {
		return url;
	}
	
	public static String getCode() {
		return code;
	}
	
	public static boolean confirm(String action) throws Exception {
		System.out.println("Server: " + RemoteConfig.getUrl());
		System.out.print(action + "? Type \"yes\" to confirm: ");
        InputStreamReader converter = new InputStreamReader(System.in);
        BufferedReader in = new BufferedReader(converter);
        String confirmation = in.readLine();
        if(confirmation.equalsIgnoreCase("yes")) {
        	return true;
        } else {
        	return false;
        }
	}
	
}
