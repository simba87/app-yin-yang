/*
 * Copyright (C) 2018 Naoghuman
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.naoghuman.app.yy.taichi;

import com.github.naoghuman.app.yy.configuration.ConfigurationEvent;
import com.github.naoghuman.app.yy.configuration.ConfigurationI18N;
import com.github.naoghuman.app.yy.configuration.ConfigurationPreferences;
import com.github.naoghuman.app.yy.configuration.ConfigurationTaiChi;
import com.github.naoghuman.app.yy.i18n.I18NProvider;
import com.github.naoghuman.lib.action.core.ActionHandlerFacade;
import com.github.naoghuman.lib.action.core.RegisterActions;
import com.github.naoghuman.lib.i18n.core.I18NBindingBuilder;
import com.github.naoghuman.lib.i18n.core.I18NMessageBuilder;
import com.github.naoghuman.lib.logger.core.LoggerFacade;
import com.github.naoghuman.lib.preferences.core.PreferencesFacade;
import java.util.Optional;
import java.util.Random;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.beans.binding.StringBinding;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

/**
 *
 * @author Naoghuman
 * @since  0.4.0
 */
final class DefaultTaiChiTerms implements 
        ConfigurationEvent, ConfigurationI18N, ConfigurationPreferences,
        ConfigurationTaiChi, RegisterActions, TaiChiTerms
{
    private static final char   TERM_SEPERATOR = ';'; // NOI18N
    private static final double OPACITY__TERM  = 1.0d;
    private static final double OPACITY__ZERO  = 0.0d;
    
    private static final Random RANDOM = new Random();
    
    private double diameterTheOne  = PREF__TAICHI_SYMBOL__DIAMETER_DEFAULT_VALUE;
    private int    termIndex       = 0;
    private int    termMaxQuantity = 0;
    
    private HBox  hbTaiChiTerms;
    private Label lYangTerm;
    private Label lYinTerm;
    
    public DefaultTaiChiTerms() {
        
    }
    
    @Override
    public void initialize() {
        LoggerFacade.getDefault().info(this.getClass(), "DefaultTaiChiTerms.initialize()"); // NOI18N
        
        diameterTheOne  = PreferencesFacade.getDefault().getDouble(PREF__TAICHI_SYMBOL__DIAMETER, PREF__TAICHI_SYMBOL__DIAMETER_DEFAULT_VALUE);
        termMaxQuantity = Integer.parseInt(I18NProvider.getDefault().getI18NFacade().getMessage(I18N__TAICHI__TERM_QUANTITY));
    }
    
    /**
     * 1) Start delay
     * 2) Blend in   YinTerm
     * 3) Delay show YinTerm
     * 4) Blend out  YinTerm
     * 5) Switch to  YangTerm
     * 6) Blend in   YangTerm
     * 7) Delay show YangTerm
     * 8) Blend out  YangTerm
     * 9) Like 1)
     */
    private SequentialTransition createSequentialTransition() {
        // Comment out to avoid spawning messages
//        LoggerFacade.getDefault().debug(this.getClass(), "YinYangTerms.createSequentialTransition()"); // NOI18N
        
        final SequentialTransition st = new SequentialTransition();
        termIndex = RANDOM.nextInt(termMaxQuantity) + 1;

        // 1
        PauseTransition pt = new PauseTransition();
        pt.setDuration(Duration.millis(5000.0d + RANDOM.nextInt(15000)));
        pt.setOnFinished((event) -> {
            hbTaiChiTerms.getChildren().clear();
            hbTaiChiTerms.setAlignment(Pos.CENTER_RIGHT);
            hbTaiChiTerms.getChildren().add(lYinTerm);
            
            final Optional<StringBinding> optional = I18NBindingBuilder.bind()
                    .callable(() -> 
                            this.extractYinTerm(I18NMessageBuilder.message()
                                    .key(String.format(I18N__TAICHI__TERM_NR, termIndex))
                                    .build()
                    ))
                    .build();
            if (optional.isPresent()) {
                lYinTerm.textProperty().bind(optional.get());
            }
        });
        st.getChildren().add(pt);
        
        // 2
        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO,           new KeyValue(lYinTerm.opacityProperty(), OPACITY__ZERO)),
                new KeyFrame(Duration.millis(500.0d), new KeyValue(lYinTerm.opacityProperty(), OPACITY__TERM)));
        st.getChildren().add(tl);
        
        // 3
        final double durationShowTerm = 5000.0d + RANDOM.nextInt(15000);
        pt = new PauseTransition();
        pt.setDuration(Duration.millis(durationShowTerm));
        st.getChildren().add(pt);
        
        // 4
        tl = new Timeline(
                new KeyFrame(Duration.ZERO,           new KeyValue(lYinTerm.opacityProperty(), OPACITY__TERM)),
                new KeyFrame(Duration.millis(500.0d), new KeyValue(lYinTerm.opacityProperty(), OPACITY__ZERO)));
        st.getChildren().add(tl);
        
        // 5
        pt = new PauseTransition();
        pt.setDuration(Duration.millis(750.0d));
        pt.setOnFinished((event) -> {
            hbTaiChiTerms.getChildren().clear();
            hbTaiChiTerms.setAlignment(Pos.CENTER_LEFT);
            hbTaiChiTerms.getChildren().add(lYangTerm);
            
            final Optional<StringBinding> optional = I18NBindingBuilder.bind()
                    .callable(() -> 
                            this.extractYangTerm(I18NMessageBuilder.message()
                                    .key(String.format(I18N__TAICHI__TERM_NR, termIndex))
                                    .build()
                    ))
                    .build();
            if (optional.isPresent()) {
                lYangTerm.textProperty().bind(optional.get());
            }
        });
        st.getChildren().add(pt);
        
        // 6
        tl = new Timeline(
                new KeyFrame(Duration.ZERO,           new KeyValue(lYangTerm.opacityProperty(), OPACITY__ZERO)),
                new KeyFrame(Duration.millis(500.0d), new KeyValue(lYangTerm.opacityProperty(), OPACITY__TERM)));
        st.getChildren().add(tl);
        
        // 7
        pt = new PauseTransition();
        pt.setDuration(Duration.millis(durationShowTerm));
        st.getChildren().add(pt);
        
        // 8
        tl = new Timeline(
                new KeyFrame(Duration.ZERO,           new KeyValue(lYangTerm.opacityProperty(), OPACITY__TERM)),
                new KeyFrame(Duration.millis(500.0d), new KeyValue(lYangTerm.opacityProperty(), OPACITY__ZERO)));
        st.getChildren().add(tl);
        
        return st;
    }
    
    private String extractYangTerm(final String termYinYang) {
        return termYinYang.substring(termYinYang.indexOf(TERM_SEPERATOR) + 1, termYinYang.length());
    }
    
    private String extractYinTerm(final String termYinYang) {
        return termYinYang.substring(0, termYinYang.indexOf(TERM_SEPERATOR));
    }

    private void onActionStartTaiChiTerms() {
        // Comment out to avoid spawning messages
//        LoggerFacade.getDefault().debug(this.getClass(), "YinYangTerms.onActionStartTaiChiTerms()"); // NOI18N
        
        final SequentialTransition st = this.createSequentialTransition();
        st.setOnFinished((event) -> {
            this.onActionStartTaiChiTerms();
        });
        
        st.playFromStart();
    }

    @Override
    public void register() {
        LoggerFacade.getDefault().info(this.getClass(), "DefaultTaiChiTerms.register()"); // NOI18N
        
        this.registerOnActionStartTaiChiTerms();
    }
    
    @Override
    public void register(final HBox hbTaiChiTerms, final Label lYinTerm, final Label lYangTerm) {
        LoggerFacade.getDefault().info(this.getClass(), "DefaultTaiChiTerms.register(HBox, Label, Label)"); // NOI18N
        
        this.hbTaiChiTerms = hbTaiChiTerms;
        this.lYinTerm      = lYinTerm;
        this.lYangTerm     = lYangTerm;
        
        this.registerComponentTaiChiTerms();
        this.registerComponentYangTerm();
        this.registerComponentYinTerm();
    }
    
    private void registerComponentTaiChiTerms() {
        LoggerFacade.getDefault().info(this.getClass(), "DefaultTaiChiTerms.registerComponentTaiChiTerms()"); // NOI18N
        
        hbTaiChiTerms.getChildren().clear();
//        hbYinYangTerms.setPrefHeight(PREF__YIN_YANG__SYMBOL_DIAMETER_DEFAULT_VALUE / 8.0d / 2.0d + YIN_YANG_SYMBOLE__OUTER_BORDER * 2.0d);
        hbTaiChiTerms.setPrefWidth(diameterTheOne - diameterTheOne / 4.0d);
        hbTaiChiTerms.setLayoutX(diameterTheOne / 8.0d + TAICHI_SYMBOL__OUTER_BORDER);
    }
    
    private void registerComponentYangTerm() {
        LoggerFacade.getDefault().info(this.getClass(), "DefaultTaiChiTerms.registerComponentYangTerm()"); // NOI18N
        
        lYangTerm.setAlignment(Pos.CENTER);
        lYangTerm.setOpacity(OPACITY__ZERO);
        lYangTerm.setPrefWidth(diameterTheOne / 2.0d);
    }
    
    private void registerComponentYinTerm() {
        LoggerFacade.getDefault().info(this.getClass(), "DefaultTaiChiTerms.registerComponentYinTerm()"); // NOI18N
        
        lYinTerm.setAlignment(Pos.CENTER);
        lYinTerm.setOpacity(OPACITY__ZERO);
        lYinTerm.setPrefWidth(diameterTheOne / 2.0d);
    }

    private void registerOnActionStartTaiChiTerms() {
        LoggerFacade.getDefault().info(this.getClass(), "DefaultTaiChiTerms.registerOnActionStartTaiChiTerms()"); // NOI18N
        
        ActionHandlerFacade.getDefault().register(
                ON_ACTION__START_TAICHI_TERMS,
                (ActionEvent event) -> {
                    this.onActionStartTaiChiTerms();
                });
    }
    
}
