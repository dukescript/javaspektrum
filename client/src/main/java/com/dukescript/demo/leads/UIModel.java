package com.dukescript.demo.leads;

import com.dukescript.demo.leads.js.Dialogs;
import com.dukescript.demo.leads.js.TemplateRegistration;
import com.dukescript.demo.leads.shared.Contact;
import com.dukescript.demo.leads.shared.Phone;
import com.dukescript.demo.leads.shared.PhoneType;
import java.util.ArrayList;
import java.util.List;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.ModelOperation;
import net.java.html.json.Models;
import net.java.html.json.OnReceive;
import net.java.html.json.Property;

/**
 * Generates UI class that provides the application logic model for the HTML
 * page.
 */
@Model(className = "UI", targetId = "", properties = {
    @Property(name = "url", type = String.class),
    @Property(name = "message", type = String.class),
    @Property(name = "contacts", type = Contact.class, array = true),
    @Property(name = "selected", type = Contact.class),
    @Property(name = "edited", type = Contact.class),
    @Property(name = "filters", type = Filter.class, array = true),
})
final class UIModel {

    // Computed Propertise
    @ComputedProperty
    public static List<Contact> filtered(List<Contact> contacts, List<Filter> filters) {
        List<Contact> filtered = new ArrayList<>(contacts);
        if (filters.isEmpty()) {
            return contacts;
        }
        for (Contact contact : contacts) {
            for (Filter filter : filters) {
                if (filter.isActive() &! contact.getTags().contains(filter.getTerm())) {
                    filtered.remove(contact);
                }
            }
        }
        return filtered;
    }

    
    //
    // REST API callbacks
    //
    @OnReceive(url = "{url}", onError = "cannotConnect")
    static void loadContacts(UI ui, List<Contact> arr) {
        ui.getContacts().clear();
        ui.getContacts().addAll(arr);
        ui.setMessage("Loaded " + arr.size() + " contact(s).");
    }

    @OnReceive(method = "POST", url = "{url}", data = Contact.class, onError = "cannotConnect")
    static void addContact(UI ui, List<Contact> updatedOnes) {
        ui.getContacts().clear();
        ui.getContacts().addAll(updatedOnes);
        ui.setSelected(null);
        ui.setEdited(null);
    }

    @OnReceive(method = "PUT", url = "{url}/{id}", data = Contact.class, onError = "cannotConnect")
    static void updateContact(UI ui, List<Contact> updatedOnes, Contact original) {
        ui.getContacts().clear();
        ui.getContacts().addAll(updatedOnes);
        ui.setMessage("Updated " + original.getLastName() + ". " + updatedOnes.size() + " contact(s) now.");
        ui.setSelected(null);
        ui.setEdited(null);
    }

    @OnReceive(method = "DELETE", url = "{url}/{id}", onError = "cannotConnect")
    static void deleteContact(UI ui, List<Contact> remainingOnes) {
        ui.getContacts().clear();
        ui.getContacts().addAll(remainingOnes);
        ui.setSelected(null);
        ui.setEdited(null);
    }

    static void cannotConnect(UI data, Exception ex) {
        data.setMessage("Cannot connect " + ex.getMessage() + ". Should not you start the server project first?");
    }

    //
    // UI callback bindings
    //
    @ModelOperation
    @Function
    static void connect(UI data) {
        final String u = data.getUrl();
        if (u.endsWith("/")) {
            data.setUrl(u.substring(0, u.length() - 1));
        }
        data.loadContacts(data.getUrl());
    }

    @Function
    static void toggleFilter(UI ui, Filter data) {
        data.setActive(!data.isActive());
    }
    

    @Function
    static void addNew(UI ui) {
        final Contact c = new Contact();
        c.getPhones().add(new Phone("+420 000 000 000", PhoneType.HOME));
        ui.setSelected(c);
        ui.setEdited(c);
    }

    @Function
    static void select(UI ui, Contact data) {
        ui.setSelected(data);
        ui.setEdited(null);
    }

    @Function
    static void edit(UI ui, Contact data) {
        ui.setSelected(data);
        ui.setEdited(data.clone());
    }

    @Function
    static void delete(UI ui, Contact data) {
        ui.deleteContact(ui.getUrl(), data.getId());
    }

    @Function
    static void cancel(UI ui) {
        ui.setEdited(null);
        ui.setSelected(null);
    }

    @Function
    static void commit(UI ui) {
        final Contact e = ui.getEdited();
        if (e == null) {
            return;
        }
        String invalid = null;
        if (e.getValidate() != null) {
            invalid = e.getValidate();
        } else if (e.getAddress().getValidate() != null) {
            invalid = e.getAddress().getValidate();
        } else {
            for (Phone p : e.getPhones()) {
                if (p.getValidate() != null) {
                    invalid = p.getValidate();
                    break;
                }
            }
        }
        if (invalid != null && !Dialogs.confirm("Not all data are valid ("
                + invalid + "). Do you want to proceed?", null
        )) {
            return;
        }

        final Contact s = ui.getSelected();
        if (s != null) {
            ui.updateContact(ui.getUrl(), s.getId(), e, e);
        } else {
            ui.addContact(ui.getUrl(), e);
        }
    }

    @Function
    static void addPhoneEdited(UI ui) {
        final List<Phone> phones = ui.getEdited().getPhones();
        PhoneType t = PhoneType.values()[phones.size() % PhoneType.values().length];
        phones.add(new Phone("", t));
    }

    @Function
    static void removePhoneEdited(UI ui, Phone data) {
        ui.getEdited().getPhones().remove(data);
    }

    private static UI uiModel;

    /**
     * Called when the page is ready.
     */
    static void onPageLoad() throws Exception {
        uiModel = new UI();
        Models.toRaw(uiModel);
        TemplateRegistration.register("edit-template", "edit-template.html");
        TemplateRegistration.register("view-template", "view-template.html");
        final String baseUrl = "http://localhost:8080/leadsserver-1.0-SNAPSHOT/webresources/contacts/";
        uiModel.setUrl(baseUrl);
        uiModel.setEdited(null);
        uiModel.setSelected(null);
        uiModel.getFilters().add(new Filter("Lead", false));
        uiModel.getFilters().add(new Filter("Dukescript",false));
        uiModel.getFilters().add(new Filter("Phone",false));
        uiModel.applyBindings();
        uiModel.connect();
    }
    
    @Model(className = "Filter", properties = {
        @Property(name = "term", type = String.class),
        @Property(name = "active", type = boolean.class)
    })
     static class FilterVMD{}

}
