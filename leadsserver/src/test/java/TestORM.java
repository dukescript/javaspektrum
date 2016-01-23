/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.dukescript.demo.leads.shared.Address;
import com.dukescript.demo.leads.shared.Contact;
import com.dukescript.demo.leads.shared.Phone;
import com.dukescript.demo.leads.shared.PhoneType;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.Test;

/**
 *
 * @author antonepple
 */
public class TestORM {

    public TestORM() {
    }

//    @Test
//    public void crud() {
//        EntityManagerFactory factory = Persistence.createEntityManagerFactory("dukeDB");
//        EntityManager em = factory.createEntityManager();
//        em.getTransaction().begin();
//
//        Contact contact = new Contact(
//                "000",
//                "Anton", "Epple",
//                new Address("Bergmannstrasse 66", "80339 München"),
//                new Phone("+49 89 54043186", PhoneType.WORK)
//        );
//
//        em.persist(contact);
//
//        em.getTransaction().commit();
//        
//       
//        em.close();
//    }
}
