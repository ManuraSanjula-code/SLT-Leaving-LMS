package com.slt.peotv.userservice.lms.service;

import com.slt.peotv.userservice.lms.shared.dto.AddressDTO;

import java.util.List;

public interface AddressService {
    public List<AddressDTO> getAddresses(String userId);
    public AddressDTO getAddress(String addressId);
}
