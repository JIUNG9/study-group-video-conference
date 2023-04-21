package com.studygroup.service.email;

import com.studygroup.entity.EmailToken;
import com.studygroup.enums.TokenType;
import com.studygroup.repository.EmailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.LongToIntFunction;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Qualifier("CheckResetPasswordTokenAlreadySent")
public class CheckResetPasswordTokenAlreadySent implements CheckTokenAlreadySent {

    private final EmailRepository emailRepo;

    @Override
    public void checkTokenSentIfSoDelete(Long memberId) {

        List<EmailToken> emailTokenList = emailRepo.findByMember_Id(memberId);
        List<Long> passwordResetTokens = emailTokenList.
                                                stream().
                                                filter(s->s.getTokenType().equals(TokenType.PASSWORD_RESET_TOKEN)).
                                                map(s->s.getId()).collect(Collectors.toList());

        if(passwordResetTokens.size()!=0) {
            for (Long id : passwordResetTokens) {
                emailRepo.deleteById(id
                );
            }
        }
    }
}