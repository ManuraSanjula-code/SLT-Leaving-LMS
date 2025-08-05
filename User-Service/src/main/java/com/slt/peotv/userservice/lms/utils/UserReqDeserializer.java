package com.slt.peotv.userservice.lms.utils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.slt.peotv.userservice.lms.shared.dto.AddressDTO;
import com.slt.peotv.userservice.lms.shared.model.request.UserReq;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class UserReqDeserializer extends StdDeserializer<UserReq> {

    public UserReqDeserializer() {
        this(null);
    }

    public UserReqDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public UserReq deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);
        UserReq userReq = new UserReq();

        if (node.has("userId")) userReq.setUserId(node.get("userId").asText());
        if (node.has("firstName")) userReq.setFirstName(node.get("firstName").asText());
        if (node.has("lastName")) userReq.setLastName(node.get("lastName").asText());
        if (node.has("email")) userReq.setEmail(node.get("email").asText());
        if (node.has("employeeId")) userReq.setEmployeeId(node.get("employeeId").asText());
        if (node.has("sltId")) userReq.setSltId(node.get("sltId").asText());
        if (node.has("password")) userReq.setPassword(node.get("password").asText());
        if (node.has("phone")) userReq.setPhone(node.get("phone").asText());
        if (node.has("gender")) userReq.setGender(node.get("gender").asText());
        if (node.has("isSltEmp")) userReq.setIsSltEmp(node.get("isSltEmp").asInt());
        if (node.has("isSltIntern")) userReq.setIsSltIntern(node.get("isSltIntern").asInt());
        if (node.has("active")) userReq.setActive(node.get("active").asInt());
        if (node.has("roaster")) userReq.setRoaster(node.get("roaster").asBoolean());

        if (node.has("roles")) {
            userReq.setRoles(parseStringList(node.get("roles")));
        }
        if (node.has("sections")) {
            userReq.setSections(parseStringList(node.get("sections")));
        }
        if (node.has("profiles")) {
            userReq.setProfiles(parseStringList(node.get("profiles")));
        }
        if (node.has("Authorities")) {
            userReq.setAuthorities(parseStringList(node.get("Authorities")));
        }
        if (node.has("deleteAddresses")) {
            userReq.setDeleteAddresses(parseStringList(node.get("deleteAddresses")));
        }

        if (node.has("joiningDate") && !node.get("joiningDate").isNull()) {
            String dateStr = node.get("joiningDate").asText();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date joiningDate = null;
            try {
                joiningDate = sdf.parse(dateStr);
            } catch (ParseException e) {

            }
            userReq.setJoiningDate(joiningDate);
        }

        if (node.has("addresses")) {
            List<AddressDTO> addresses = new ArrayList<>();
            for (JsonNode addrNode : node.get("addresses")) {
                AddressDTO address = new AddressDTO();
                if (addrNode.has("addressId")) address.setAddressId(addrNode.get("addressId").asText());
                if (addrNode.has("city")) address.setCity(addrNode.get("city").asText());
                if (addrNode.has("country")) address.setCountry(addrNode.get("country").asText());
                if (addrNode.has("streetName")) address.setStreetName(addrNode.get("streetName").asText());
                if (addrNode.has("postalCode")) address.setPostalCode(addrNode.get("postalCode").asText());
                if (addrNode.has("isDefault")) address.setDefault(addrNode.get("isDefault").asBoolean());
                addresses.add(address);
            }
            userReq.setAddresses(addresses);
        }

        if (node.has("additional")) {
            JsonNode additionalNode = node.get("additional");
            UserReq.Additional additional = new UserReq.Additional();

            if (additionalNode.has("addedRoles")) {
                additional.setAddedRoles(parseStringList(additionalNode.get("addedRoles")));
            }
            if (additionalNode.has("addedSelections")) {
                additional.setAddedSelections(parseStringList(additionalNode.get("addedSelections")));
            }
            if (additionalNode.has("addedProfiles")) {
                additional.setAddedProfiles(parseStringList(additionalNode.get("addedProfiles")));
            }
            if (additionalNode.has("deleteRoles")) {
                additional.setDeleteRoles(parseStringList(additionalNode.get("deleteRoles")));
            }
            if (additionalNode.has("deleteSelections")) {
                additional.setDeleteSelections(parseStringList(additionalNode.get("deleteSelections")));
            }
            if (additionalNode.has("deleteProfiles")) {
                additional.setDeleteProfiles(parseStringList(additionalNode.get("deleteProfiles")));
            }

            userReq.setAdditional(additional);
        }

        if (node.has("admins")) {
            List<String> admins = new ArrayList<>();
            JsonNode adminsNode = node.get("admins");
            if (adminsNode.isArray()) {
                for (JsonNode item : adminsNode) {
                    admins.add(item.asText());
                }
            }
            userReq.setAdmins(admins);
        }

        if (node.has("addedAdmins")) {
            List<String> addedAdmins = new ArrayList<>();
            JsonNode addedAdminsNode = node.get("addedAdmins");
            if (addedAdminsNode.isArray()) {
                for (JsonNode item : addedAdminsNode) {
                    addedAdmins.add(item.asText());
                }
            }
            userReq.setAddedAdmins(addedAdmins);
        }

        if (node.has("deletedAdmins")) {
            List<String> deletedAdmins = new ArrayList<>();
            JsonNode deletedAdminsNode = node.get("deletedAdmins");
            if (deletedAdminsNode.isArray()) {
                for (JsonNode item : deletedAdminsNode) {
                    deletedAdmins.add(item.asText());
                }
            }
            userReq.setDeletedAdmins(deletedAdmins);
        }

        return userReq;
    }

    private List<String> parseStringList(JsonNode arrayNode) {
        if (arrayNode == null || arrayNode.isNull()) {
            return new ArrayList<>();
        }
        return StreamSupport.stream(arrayNode.spliterator(), false)
                .map(JsonNode::asText)
                .collect(Collectors.toList());
    }
}
