package com.dukescript.demo.leads.server;

import com.dukescript.demo.leads.shared.Contact;
import com.dukescript.demo.leads.shared.Phone;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/contacts/")
@Singleton
public final class ContactsResource {

    private final List<Contact> contacts = new CopyOnWriteArrayList<>();
    private int counter;
    
    @Inject
    EntityManager entityManager;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Contact> allContacts() {
        List<Contact> contacts = null;
        Query query = entityManager.createNamedQuery("findAllContacts");
        contacts = query.getResultList();
        return contacts;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public Contact getContact(@PathParam("id") String id) {
        Query query = entityManager.createNamedQuery("findContactById");
        query.setParameter("id", id);
        Contact contact = (Contact) query.getSingleResult();
        return contact;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Contact> addContact(Contact c) {
        validate(c);
        List<Contact> contacts = null;
        entityManager.getTransaction().begin();
        entityManager.persist(c);
        entityManager.getTransaction().commit();
        Query query = entityManager.createNamedQuery("findAllContacts");
        contacts = query.getResultList();
        return contacts;
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public List<Contact> removeContact(@PathParam("id") String id) {
        List<Contact> contacts = null;
        Query query = entityManager.createNamedQuery("findContactById");
        query.setParameter("id", id);
        Contact contact = (Contact) query.getSingleResult();
        entityManager.getTransaction().begin();
        entityManager.remove(contact);
        entityManager.getTransaction().commit();
        query = entityManager.createNamedQuery("findAllContacts");
        contacts = query.getResultList();
        return contacts;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public List<Contact> updateContact(@PathParam("id") String id, Contact newContact) {
        List<Contact> contacts = null;
        entityManager.getTransaction().begin();
        Contact merged = entityManager.merge(newContact);
        entityManager.flush();
        String lastName = merged.getLastName();
        entityManager.getTransaction().commit();
        Logger.getLogger(ContactsResource.class.getName()).severe("######" + lastName);

        Query query = entityManager.createNamedQuery("findAllContacts");
        contacts = query.getResultList();
        return contacts;
    }

    private static void validate(Contact c) {
        if (c.getValidate() != null) {
            throw new NotAcceptableException(c.getValidate());
        }
        if (c.getAddress().getValidate() != null) {
            throw new NotAcceptableException(c.getAddress().getValidate());
        }
        for (Phone phone : c.getPhones()) {
            String err = phone.getValidate();
            if (err != null) {
                throw new javax.ws.rs.NotAcceptableException(err);
            }
        }
    }
}
