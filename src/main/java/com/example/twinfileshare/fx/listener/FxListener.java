package com.example.twinfileshare.fx.listener;

import com.example.twinfileshare.event.payload.AccountDisconnectedEvent;
import com.example.twinfileshare.fx.presenter.EmailSharePresenter;
import com.example.twinfileshare.fx.presenter.LinkSharePresenter;
import com.example.twinfileshare.fx.presenter.ManagePresenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class FxListener {

    @Autowired
    private LinkSharePresenter linkSharePresenter;
    @Autowired
    private EmailSharePresenter emailSharePresenter;
    @Autowired
    private ManagePresenter managePresenter;

    @EventListener
    public void handleAccountDisconnectedEvent(AccountDisconnectedEvent event) {
        var email = event.getEmail();
        linkSharePresenter.removeAccountFromChoiceBox(email);
        emailSharePresenter.removeAccountFromChoiceBox(email);
        managePresenter.removeAccountFromChoiceBox(email);
    }
}
