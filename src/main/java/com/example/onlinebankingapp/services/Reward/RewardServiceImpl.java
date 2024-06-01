package com.example.onlinebankingapp.services.Reward;

import com.example.onlinebankingapp.dtos.RewardDTO;
import com.example.onlinebankingapp.entities.AccountRewardEntity;
import com.example.onlinebankingapp.entities.PaymentAccountEntity;
import com.example.onlinebankingapp.entities.RewardEntity;
import com.example.onlinebankingapp.entities.SavingAccountEntity;
import com.example.onlinebankingapp.enums.RewardType;
import com.example.onlinebankingapp.exceptions.DataNotFoundException;
import com.example.onlinebankingapp.exceptions.InvalidParamException;
import com.example.onlinebankingapp.repositories.AccountRewardRepository;
import com.example.onlinebankingapp.repositories.RewardRepository;
import com.example.onlinebankingapp.services.PaymentAccount.PaymentAccountService;
import com.example.onlinebankingapp.utils.ImageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RewardServiceImpl implements RewardService {
    private final RewardRepository rewardRepository;
    private final PaymentAccountService paymentAccountService;
    private final AccountRewardRepository accountRewardRepository;
    @Override
    public RewardEntity insertReward(RewardDTO rewardDTO) throws InvalidParamException {
        String dataValidationResult = isRewardDTOValid(rewardDTO);
        if(!dataValidationResult.equals("OK")){
            throw new InvalidParamException(dataValidationResult);
        }

        RewardEntity newRewardEntity = RewardEntity.builder()
                .rewardName(rewardDTO.getRewardName())
                .rewardType(RewardType.valueOf(rewardDTO.getRewardType()))
                .costPoint(rewardDTO.getCostPoint())
                .build();

        return rewardRepository.save(newRewardEntity);
    }

    @Override
    public RewardEntity getRewardById(Long id) throws Exception {
        if(id == null){
            throw new Exception("Missing parameter");
        }

        RewardEntity queryRewardEntity = rewardRepository.findRewardEntityById(id);
        if(queryRewardEntity != null) {
            return queryRewardEntity;
        }
        throw new DataNotFoundException("Cannot find reward with Id: "+ id);
    }

    @Override
    public List<RewardEntity> getAllRewards() throws Exception {
        return rewardRepository.findAll();
    }

    @Override
    public RewardEntity updateReward(Long id, RewardDTO rewardDTO) throws Exception {
        String dataValidationResult = isRewardDTOValid(rewardDTO);
        if(!dataValidationResult.equals("OK")){
            throw new InvalidParamException(dataValidationResult);
        }

        if(id == null){
            throw new Exception("Missing parameter");
        }

        RewardEntity editedRewardEntity = rewardRepository.findRewardEntityById(id);

        editedRewardEntity.setRewardName(rewardDTO.getRewardName());
        editedRewardEntity.setCostPoint(rewardDTO.getCostPoint());
        editedRewardEntity.setRewardType(RewardType.valueOf(rewardDTO.getRewardType()));

        return rewardRepository.save(editedRewardEntity);
    }

    @Override
    public RewardEntity uploadRewardImg(Long id, MultipartFile file) throws Exception {
        RewardEntity editedRewardEntity = rewardRepository.findRewardEntityById(id);
        if(editedRewardEntity == null){
            throw new DataNotFoundException("Cannot find reward with Id: "+ id);
        }

        if(!ImageUtils.isImgValid(file)){
            throw new InvalidParamException("Image must be less than 2mbs and in the following formats: jpeg, jpg, png, webp");
        }

        editedRewardEntity.setImage(file.getBytes());
        return rewardRepository.save(editedRewardEntity);
    }

    @Override
    public List<AccountRewardEntity> getUserAccountRewards(Long userId) throws DataNotFoundException {
        List<PaymentAccountEntity> userPaymentAccountsList = paymentAccountService.getPaymentAccountsByCustomerId(userId);
        List<AccountRewardEntity> userAccountRewardsList = new ArrayList<>();
        for (PaymentAccountEntity paymentAccount : userPaymentAccountsList) {
//            List<AccountRewardEntity> accountRewardList
//                    = accountRewardRepository.findAccountRewardEntitiesByAccountRewardKeyPaymentAccount(paymentAccount);
//            List<AccountRewardEntity.AccountRewardRelationshipKey> rewardRelationshipKeys = accountRewardList.stream().map(AccountRewardEntity::getAccountRewardKey).toList();
//            List<RewardEntity> rewardsOfPaymentAccount = rewardRelationshipKeys.stream().map(AccountRewardEntity.AccountRewardRelationshipKey::getReward).toList();
//            userRewardsList.addAll(rewardsOfPaymentAccount);
            userAccountRewardsList.addAll(accountRewardRepository.findAccountRewardEntitiesByAccountRewardKeyPaymentAccount(paymentAccount));
        }

        return userAccountRewardsList;
    }

    private String isRewardDTOValid(RewardDTO rewardDTO) {
        if (StringUtils.isBlank(rewardDTO.getRewardName())
                || StringUtils.isBlank(rewardDTO.getRewardType())) {
            return "Missing parameters";
        }

        try {
            RewardType rewardType = RewardType.valueOf(rewardDTO.getRewardType());
        } catch (IllegalArgumentException e){
            return "Invalid reward type";
        }

        if(rewardDTO.getCostPoint() <= 0 || rewardDTO.getCostPoint() > 9999){
            return "Reward cost must be between 1 and 9999";
        }

        return "OK";
    }
}
