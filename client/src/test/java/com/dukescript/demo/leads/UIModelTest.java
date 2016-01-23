package com.dukescript.demo.leads;

import com.dukescript.demo.leads.shared.Contact;
import com.dukescript.demo.leads.shared.PhoneType;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.java.html.BrwsrCtx;
import net.java.html.json.Models;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.spi.JSONCall;
import org.netbeans.html.json.spi.Transfer;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class UIModelTest {

    @Test
    public void addNewSetsEdited() {
        UI model = new UI();
        Contact c = new Contact();
        UIModel.edit(model, c);
        assertEquals(model.getEdited(), c, "c is now edited");

        assertTrue(model.getEdited().getPhones().isEmpty(), "No phone yet");
        UIModel.addPhoneEdited(model);
        assertEquals(model.getEdited().getPhones().size(), 1, "One phone added");
        assertEquals(model.getEdited().getPhones().get(0).getType(), PhoneType.HOME, "First is home phone");

        UIModel.addPhoneEdited(model);
        assertEquals(model.getEdited().getPhones().size(), 2, "2nd phone added");
        assertEquals(model.getEdited().getPhones().get(1).getType(), PhoneType.WORK, "2nd is work phone");
    }

    @Test
    public void connectedTest() {
        class MockTrans implements Transfer {
            
            @Override
            public void extract(Object obj, String[] props, Object[] values) {
                Map<?, ?> mt = (Map<?, ?>) obj;
                for (int i = 0; i < props.length; i++) {
                    values[i] = mt.get(props[i]);
                }
            }

            @Override
            public Object toJSON(InputStream is) throws IOException {
                fail("Not implemented");
                return null;
            }

            @Override
            public void loadJSON(JSONCall call) {
                assertFalse(call.isJSONP(), "Regular JSON call, not JSONP");
                assertEquals(call.composeURL(null), "http://localhost:8080/leadsserver-1.0-SNAPSHOT/webresources/contacts/", "The expected URL queried");
                HashMap result = new HashMap<>();
                result.put("firstName", "Toni");
                result.put("lastName", "Epple");
                call.notifySuccess(result);
            }
        }
        
        MockTrans mock = new MockTrans();
        //  Der Mock Transfer wird registriert
        BrwsrCtx ctx = Contexts.newBuilder()
                .register(Transfer.class, mock, 1)
                .build();
        
        UI uiModel = Models.bind(new UI(), ctx);
        final String baseUrl = "http://localhost:8080/leadsserver-1.0-SNAPSHOT/webresources/contacts/";
        uiModel.setUrl(baseUrl);
        assertEquals(0, uiModel.getContacts().size());
        uiModel.loadContacts(baseUrl);
        assertEquals(1, uiModel.getContacts().size());
        Contact contact = uiModel.getContacts().get(0);
        assertEquals("Toni", contact.getFirstName());     
    }
}
