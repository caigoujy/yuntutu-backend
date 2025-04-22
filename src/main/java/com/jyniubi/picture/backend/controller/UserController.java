package com.jyniubi.picture.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jyniubi.picture.backend.annotation.AuthCheck;
import com.jyniubi.picture.backend.common.BaseResponse;
import com.jyniubi.picture.backend.common.DeleteRequest;
import com.jyniubi.picture.backend.common.ResultUtils;
import com.jyniubi.picture.backend.constant.UserConstant;
import com.jyniubi.picture.backend.exception.BusinessException;
import com.jyniubi.picture.backend.exception.ErrorCode;
import com.jyniubi.picture.backend.exception.ThrowUtils;
import com.jyniubi.picture.backend.model.dto.user.*;
import com.jyniubi.picture.backend.model.entity.User;
import com.jyniubi.picture.backend.model.vo.UserLoginVo;
import com.jyniubi.picture.backend.model.vo.UserVO;
import com.jyniubi.picture.backend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     * @param registerRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> registerUser(@RequestBody UserRegisterRequest registerRequest) {
        ThrowUtils.throwIf(registerRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = registerRequest.getUserAccount();
        String userPassword = registerRequest.getUserPassword();
        String checkPassword = registerRequest.getCheckPassword();
        long userId = this.userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(userId);
    }

    /**
     * 用户登录
     * @param loginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<UserLoginVo> loginUser(@RequestBody UserLoginRequest loginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(loginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = loginRequest.getUserAccount();
        String userPassword = loginRequest.getUserPassword();
        UserLoginVo userLoginVo = this.userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(userLoginVo);
    }

    /**
     * 用户注销
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> logoutUser(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean flag = this.userService.userLogout(request);
        return ResultUtils.success(flag);
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/get/login")
    public BaseResponse<UserLoginVo> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getUserLogin(request);
        return ResultUtils.success(userService.getUserLoginVo(loginUser));
    }

    /**
     * 添加用户
     * @param addRequest
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest addRequest) {
        ThrowUtils.throwIf(addRequest == null, ErrorCode.PARAMS_ERROR);
        User userAccount = this.userService.getOne(new QueryWrapper<User>().eq("userAccount", addRequest.getUserAccount()));
        ThrowUtils.throwIf(userAccount != null, ErrorCode.USER_EXIST_ERROR);
        User user = new User();
        BeanUtils.copyProperties(addRequest,user);
        final String DEFAULT_PASSWORD = "12345678";
        String encryptPwd = this.userService.getEncryptPwd(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPwd);
        if (addRequest.getUserName().isEmpty()) {
            user.setUserName("云图库_" + UUID.randomUUID().toString().substring(0, 8));
        }
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据userId查询用户(管理员)
     * @param id
     * @return
     */
    @GetMapping("/queryUserById")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> queryUserById(@RequestParam("id") long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = this.userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据userId查询用户（脱敏）
     * @param id
     * @return
     */
    @GetMapping("/queryUserVoById")
    public BaseResponse<UserVO> queryUserVoById(@RequestParam("id") long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        BaseResponse<User> userBaseResponse = queryUserById(id);
        User user = userBaseResponse.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 根据id删除用户信息
     * @param deleteRequest
     * @return
     */
    @PostMapping("/deleteUser")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     *  更新用户信息
     * @param updateRequest
     * @return
     */
    @PostMapping("updateUser")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest updateRequest) {
        if (updateRequest == null || updateRequest.getId() == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(updateRequest,user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return  ResultUtils.success(true);
    }


    /**
     *  分页查询用户列表
     * @param queryRequest
     * @return
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVoByPage(@RequestBody UserQueryRequest queryRequest){
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        int current = queryRequest.getCurrent();
        int pageSize = queryRequest.getPageSize();
        Page<User> userPage = this.userService.page(new Page<>(current, pageSize), userService.getUserQueryWrapper(queryRequest));
        ThrowUtils.throwIf(userPage == null, ErrorCode.PARAMS_ERROR);
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
        List<UserVO> userVOList = this.userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }
}
