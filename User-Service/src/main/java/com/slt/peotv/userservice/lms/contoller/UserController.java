package com.slt.peotv.userservice.lms.contoller;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import com.slt.peotv.userservice.lms.entity.TempUser;
import com.slt.peotv.userservice.lms.repository.*;
import com.slt.peotv.userservice.lms.shared.dto.*;
import com.slt.peotv.userservice.lms.shared.model.request.*;
import com.slt.peotv.userservice.lms.utils.UpdateUtilsArchive;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.slt.peotv.userservice.lms.entity.UserEntity;
import com.slt.peotv.userservice.lms.exceptions.UserServiceException;
import com.slt.peotv.userservice.lms.exceptions.UserUnAuthorizedServiceException;
import com.slt.peotv.userservice.lms.service.AddressService;
import com.slt.peotv.userservice.lms.service.UserService;
import com.slt.peotv.userservice.lms.shared.model.response.AddressesRest;
import com.slt.peotv.userservice.lms.shared.model.response.ErrorMessages;
import com.slt.peotv.userservice.lms.shared.model.response.OperationStatusModel;
import com.slt.peotv.userservice.lms.shared.model.response.RequestOperationStatus;
import com.slt.peotv.userservice.lms.shared.model.response.UserRest;

@RestController
@RequestMapping("/users")
public class UserController {

	public class UserMapper {
	    public static UserDto mapToUserDto(UserDetailsRequestModel requestModel) {
	        UserDto userDto = new UserDto();

	        userDto.setUserId(requestModel.getUserId());
	        userDto.setFirstName(requestModel.getFirstName());
	        userDto.setLastName(requestModel.getLastName());
	        userDto.setEmail(requestModel.getEmail());
	        userDto.setPassword(requestModel.getPassword());
	        userDto.setProfilePic(requestModel.getProfilePic());
	        userDto.setIsSltEmp(requestModel.getIsSltEmp());
	        userDto.setIsSltIntern(requestModel.getIsSltIntern());
	        userDto.setActive(requestModel.getActive());
	        userDto.setPhone(requestModel.getPhone());
	        userDto.setGender(requestModel.getGender());
	        userDto.setRoles(requestModel.getRoles());
	        userDto.setSections(requestModel.getSections());
	        userDto.setProfiles(requestModel.getProfiles());

	        // Map AddressRequestModel list to AddressDTO list
	        List<AddressDTO> addressDTOs = requestModel.getAddresses().stream().map(UserMapper::mapToAddressDTO).collect(Collectors.toList());
	        userDto.setAddresses(addressDTOs);

	        return userDto;
	    }

	    private static AddressDTO mapToAddressDTO(AddressRequestModel addressRequestModel) {
	        AddressDTO addressDTO = new AddressDTO();
	        addressDTO.setCity(addressRequestModel.getCity());
	        addressDTO.setCountry(addressRequestModel.getCountry());
	        addressDTO.setStreetName(addressRequestModel.getStreetName());
	        addressDTO.setPostalCode(addressRequestModel.getPostalCode());
	        addressDTO.setIsDefault(addressRequestModel.getIsDefault());
	        addressDTO.setAddressId(addressRequestModel.getAddressId());
	        return addressDTO;
	    }
	    public static UserRest mapUserDtoToUserRest(UserDto userDto) {
	        if (userDto == null) {
	            return null;
	        }

	        UserRest userRest = new UserRest();
	        userRest.setUserId(userDto.getUserId());
	        userRest.setFirstName(userDto.getFirstName());
	        userRest.setLastName(userDto.getLastName());
	        userRest.setEmail(userDto.getEmail());
	        userRest.setProfilePic(userDto.getProfilePic());
	        userRest.setRoles(userDto.getRoles());
	        userRest.setSections(userDto.getSections());
	        userRest.setProfiles(userDto.getProfiles());
	        userRest.setIsSltEmp(userDto.getIsSltEmp());
	        userRest.setIsSltIntern(userDto.getIsSltIntern());
	        userRest.setActive(userDto.getActive());
	        userRest.setPhone(userDto.getPhone());
	        userRest.setGender(userDto.getGender());

	        // Convert List<AddressDto> to List<AddressesRest>
	        if (userDto.getAddresses() != null) {
	            List<AddressesRest> addressesRestList = userDto.getAddresses().stream()
	                    .map(addressDto -> {
	                        AddressesRest addressesRest = new AddressesRest();
	                        addressesRest.setAddressId(addressDto.getAddressId());
	                        addressesRest.setCity(addressDto.getCity());
	                        addressesRest.setCountry(addressDto.getCountry());
	                        addressesRest.setStreetName(addressDto.getStreetName());
	                        addressesRest.setPostalCode(addressDto.getPostalCode());
	                        addressesRest.setIsDefault(addressDto.getIsDefault());
	                        addressesRest.setAddressId(addressDto.getAddressId());
	                        return addressesRest;
	                    })
	                    .collect(Collectors.toList());

	            userRest.setAddresses(addressesRestList);
	        }

	        return userRest;
	    }
		public static AddressesRest mapToAddressRest(AddressDTO addressDTO) {
			AddressesRest addressRest = new AddressesRest();
			addressRest.setAddressId(addressDTO.getAddressId());
			addressRest.setCity(addressDTO.getCity());
			addressRest.setCountry(addressDTO.getCountry());
			addressRest.setStreetName(addressDTO.getStreetName());
			addressRest.setPostalCode(addressDTO.getPostalCode());
			addressRest.setIsDefault(addressDTO.getIsDefault());
			return addressRest;
		}
		public static AddressDTO mapToAddressDTO(AddressesRest addressRest) {
			AddressDTO addressDTO = new AddressDTO();
			addressDTO.setAddressId(addressRest.getAddressId());
			addressDTO.setCity(addressRest.getCity());
			addressDTO.setCountry(addressRest.getCountry());
			addressDTO.setStreetName(addressRest.getStreetName());
			addressDTO.setPostalCode(addressRest.getPostalCode());
			addressDTO.setIsDefault(addressRest.getIsDefault());
			return addressDTO;
		}
		public static UserDto mapToUserDto(UserRest userRest) {
			UserDto userDto = new UserDto();
			userDto.setUserId(userRest.getUserId());
			userDto.setFirstName(userRest.getFirstName());
			userDto.setLastName(userRest.getLastName());
			userDto.setEmail(userRest.getEmail());
			userDto.setProfilePic(userRest.getProfilePic());
			userDto.setRoles(userRest.getRoles());
			userDto.setSections(userRest.getSections());
			userDto.setProfiles(userRest.getProfiles());
			userDto.setIsSltEmp(userRest.getIsSltEmp());
			userDto.setIsSltIntern(userRest.getIsSltIntern());
			userDto.setActive(userRest.getActive());
			userDto.setPhone(userRest.getPhone());
			userDto.setGender(userRest.getGender());

			// Map AddressesRest to AddressDTO
			if (userRest.getAddresses() != null) {
				List<AddressDTO> addressesDto = userRest.getAddresses().stream()
						.map(UserMapper::mapToAddressDTO)
						.collect(Collectors.toList());
				userDto.setAddresses(addressesDto);
			}

			return userDto;
		}
		public static UserRest mapToUserRest(UserDto userDto) {
			UserRest userRest = new UserRest();
			userRest.setUserId(userDto.getUserId());
			userRest.setFirstName(userDto.getFirstName());
			userRest.setLastName(userDto.getLastName());
			userRest.setEmail(userDto.getEmail());
			userRest.setProfilePic(userDto.getProfilePic());
			userRest.setRoles(userDto.getRoles());
			userRest.setSections(userDto.getSections());
			userRest.setProfiles(userDto.getProfiles());
			userRest.setIsSltEmp(userDto.getIsSltEmp());
			userRest.setIsSltIntern(userDto.getIsSltIntern());
			userRest.setActive(userDto.getActive());
			userRest.setPhone(userDto.getPhone());
			userRest.setGender(userDto.getGender());

			// Map AddressDTO to AddressesRest
			if (userDto.getAddresses() != null) {
				List<AddressesRest> addressesRest = userDto.getAddresses().stream()
						.map(UserMapper::mapToAddressRest)
						.collect(Collectors.toList());
				userRest.setAddresses(addressesRest);
			}

			return userRest;
		}
	}

	@Autowired
	private UserService userService;

	@Autowired
	private AddressService addressService;

	@Autowired
	private AddressService addressesService;

	@Autowired
	private UpdateUtilsArchive updateUtils;

	@Autowired
	private TempUserRepo tempUserRepo;

	@GetMapping("/temp")
	private List<TempUser> tempUsers(){
		return (List<TempUser>) tempUserRepo.findAll();
	}

	@RequestMapping(value = "/auth/{userid}", method = {RequestMethod.POST, RequestMethod.PUT})
	public AuthorityDTO saveAuth(@RequestBody AuthReq req, @PathVariable("userid") String userid) {
		return userService.saveAuthority(req);
	}

	@DeleteMapping("/auth/{id}/{userid}")
	public ResponseEntity<Void> deleteAuth(@PathVariable("id") String id, @PathVariable("userid") String userid) {
		userService.deleteAuth(Long.parseLong(id));
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@DeleteMapping("/section/{id}/{userid}")
	public ResponseEntity<Void> deleteSection(@PathVariable("id") Long id, @PathVariable("userid") String userid) {
		userService.deleteSection(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(value = "/section/{userid}", method = {RequestMethod.POST, RequestMethod.PUT})
	public SectionDTO saveSection(@RequestBody SectionReq req, @PathVariable("userid") String userid) {
		return userService.saveSection(req);
	}

	@RequestMapping(value = "/profile/{userid}", method = {RequestMethod.POST, RequestMethod.PUT})
	public ProfilesDTO saveProfile(@RequestBody ProfileReq req, @PathVariable("userid") String userid) {
		return userService.saveProfile(req);
	}
	@DeleteMapping("/profile/{id}/{userid}")
	public ResponseEntity<Void> deleteProfile(@PathVariable("id") Long id, @PathVariable("userid") String userid) {
		userService.deleteProfile(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("/check/auth/{name}")
	public boolean checkAuth(@PathVariable("name") String name) {
		return userService.checkAuth(name);
	}

	@GetMapping(path="/names/roles")
	public List<String> getAllRoleNames() {
		return userService.getAllRoleNames();
	}

	@GetMapping(path="/names/sections")
	public List<String> getAllSectionNames() {
		return userService.getAllSectionNames();
	}

	@GetMapping(path="/names/profiles")
	public List<String> getAllProfileNames() {
		return userService.getAllProfileNames();
	}

	@GetMapping(path = "/{userid}")
	public UserRest getUser(@PathVariable String userid, Authentication authentication) {
		String name = authentication.getName();
		String[] parts = name.split(" ");

		String loginType = parts.length > 1 ? parts[1] : "DEFAULT";
		if ("TEMP".equals(loginType)) {
			return userService.getUserByUserId_(userid);
		}else {
			UserDto userDto = userService.getUserByUserId(userid);
			return UserMapper.mapToUserRest(userDto);
		}
	}
	
	@GetMapping(path = "/get-role/{name}")
	public List<UserDtoArchive> getRole(@PathVariable String name) {
		List<UserDtoArchive> userDto = userService.findByRoleName(name);
		return userDto;
	}
	
	@GetMapping("/roles")
	public List<RoleDTO> getAllRole(){
		return userService.getRole();
	}
	
	@GetMapping("/profile")
	public List<ProfilesDTO> getAllSection(){
		return userService.getProfile();
	}
	
	@GetMapping("/sections")
	public List<SectionDTO> getAllProfiles(){
		return userService.getSection();
	}
	
	@GetMapping("/authorities")
	public List<Map<String, String>> getAuthority(){
		return userService.getAuthority();
	}

	@RequestMapping(value = "/roles/{userid}", method = {RequestMethod.POST, RequestMethod.PUT})
	public RoleDTO createRoles(@RequestBody RoleReq req, @PathVariable String userid){
		return userService.saveRole(req);
	}

	@DeleteMapping("/delete/role/{id}/{userid}")
	public void deleteRole(@PathVariable Long id, @PathVariable String userid){
		userService.deleteRole(id);

	}
	@GetMapping("/check-address/{userid}")
	public boolean checkAddress(@PathVariable String userid) {
		return userService.userAddress(userid);
	}

	@PutMapping("/upload-pic/{userid}")
	public UserRest updateUserProfile(@PathVariable String userid, @RequestParam("image") MultipartFile file) throws Exception {
		UserDto createdUser = userService.updateUserProfile(file, userid);
		return UserMapper.mapToUserRest(createdUser);
	}

	@PostMapping("/add/employees/{userid}")
	public UserDto createEmployee(@PathVariable String userid, @RequestBody UserDto userDetails) throws Exception {
		return userService.createUser(userDetails);
	}

	@PutMapping(path = "/{employeeId}/{userid}")
	public UserRest updateEmployee(@PathVariable String userid, @PathVariable String employeeId, @RequestBody UserDto userDetails) {
		UserDto updateUser = updateUtils.updateUser(employeeId, userDetails);
		return UserMapper.mapUserDtoToUserRest(updateUser);
	}
	
	@PutMapping(path = "/{userid}")
	public UserRest updateUser(@PathVariable String userid, @RequestBody UserDto userDetails) throws Exception {
		UserDto updateUser = userService.updateUser(userid, userDetails);		
		return UserMapper.mapUserDtoToUserRest(updateUser);
	}

	@DeleteMapping(path = "/{userid}")
	public OperationStatusModel deleteUser(@PathVariable String userid) {
		OperationStatusModel returnValue = new OperationStatusModel();
		returnValue.setOperationName(RequestOperationName.DELETE.name());

		userService.deleteUser(userid);

		returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
		return returnValue;
	}

	@GetMapping()
	public List<UserRest> getUsers(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "limit", defaultValue = "2") int limit) {
		List<UserRest> returnValue = new ArrayList<>();

		List<UserDto> users = userService.getUsers(page, limit);

		for (UserDto userDto : users) {
			UserRest userModel = new UserRest();
			BeanUtils.copyProperties(userDto, userModel);
			returnValue.add(userModel);
		}

		return returnValue;
	}


    @GetMapping("/all")
    public Page<UserEntity> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

		return userService.getAllUsers(page, size);
    }

	@GetMapping(path = "/{addressId}/addresses", produces = { MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE})
	public CollectionModel<AddressesRest> getUserAddresses(@PathVariable String addressId) {
		List<AddressesRest> returnValue = new ArrayList<>();

		List<AddressDTO> addressesDTO = addressesService.getAddresses(addressId);

		if (addressesDTO != null && !addressesDTO.isEmpty()) {
			// Manually map AddressDTO to AddressesRest
			for (AddressDTO addressDTO : addressesDTO) {
                AddressesRest addressRest = UserMapper.mapToAddressRest(addressDTO);

				Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
								.getUserAddress(addressId, addressRest.getAddressId()))
						.withSelfRel();
				addressRest.add(selfLink);

				returnValue.add(addressRest);
			}
		}

		return CollectionModel.of(returnValue);
	}

	@GetMapping(path = "/{userId}/addresses/{addressId}", produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE})
	public EntityModel<AddressesRest> getUserAddress(@PathVariable String userId, @PathVariable String addressId) {

		AddressDTO addressesDto = addressService.getAddress(addressId);

		AddressesRest returnValue = UserMapper.mapToAddressRest(addressesDto);

		Link userLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(userId).withRel("user");
		Link userAddressesLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getUserAddresses(userId))
				.withRel("addresses");
		Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
				.getUserAddress(userId, addressId))
				.withSelfRel();


		return EntityModel.of(returnValue, Arrays.asList(userLink,userAddressesLink, selfLink));
	}

	@PostMapping("/reset-password/{userId}")
	public void resetPassWord(@RequestBody UserPasswordReset userPasswordReset, @PathVariable String userId){

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();

		if(!Objects.equals(username, userId)) {
			throw new UserUnAuthorizedServiceException(ErrorMessages.AUTHENTICATION_FAILED.getErrorMessage());
		}

		if(!userPasswordReset.getNewPassword().equals(userPasswordReset.getNewPassword())) {
			throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());
		}

		userService.resetPassWord(userPasswordReset);
	}

	@GetMapping("/image/{userid}")
	public ResponseEntity<Resource> getImage(@PathVariable String userid) {
		try {
			Resource resource = userService.getImage(userid);

			String contentType = "image/jpeg"; // Default to JPEG
			try {
				contentType = Files.probeContentType(resource.getFile().toPath());
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType(contentType))
					.body(resource);
		} catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}

}
