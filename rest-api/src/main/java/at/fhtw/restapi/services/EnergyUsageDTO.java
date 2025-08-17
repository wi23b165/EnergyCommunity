package at.fhtw.restapi.services;

public record EnergyUsageDTO(String hourIso, double communityProduced, double communityUsed, double gridUsed) {}
