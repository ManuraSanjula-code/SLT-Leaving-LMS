package com.slt.peotv.userservice.lms.service;

import java.util.List;

import com.slt.peotv.userservice.lms.shared.dto.AddressDTO;

public interface AddressService {
    public List<AddressDTO> getAddresses(String userId);
    public AddressDTO getAddress(String addressId);
}
