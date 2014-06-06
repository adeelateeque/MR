package com.codurs.meetroulette.core;

import com.codurs.meetroulette.pusher.PusherService;

import dagger.Module;

/**
 * Add all the other modules to this one.
 */
@Module
(
    includes = {
            AndroidModule.class,
            BootstrapModule.class
    },
    injects = {
            BootstrapApplication.class,
            PusherService.class,
    }
)
public class RootModule {
}