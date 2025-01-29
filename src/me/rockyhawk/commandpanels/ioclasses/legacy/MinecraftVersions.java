package me.rockyhawk.commandpanels.ioclasses.legacy;

public enum MinecraftVersions {
    v1_8( "1.8", 0 ),
    v1_9( "1.9", 1 ),
    v1_10( "1.10", 2 ),
    v1_11( "1.11", 3 ),
    v1_12( "1.12", 4 ),
    v1_13( "1.13", 5 ),
    v1_14( "1.14", 6 ),
    v1_15( "1.15", 7 ),
    v1_16( "1.16", 8 ),
    v1_17( "1.17", 9 ),
    v1_18( "1.18", 10 ),
    v1_19( "1.19", 11 ),
    v1_20( "1.20", 12 ),
    v1_21( "1.21", 13 ),
    v1_22( "1.22", 14 );


    private int order;
    private String key;

    MinecraftVersions( String key, int v ) {
        this.key = key;
        order = v;
    }

    public boolean greaterThanOrEqualTo( MinecraftVersions other ) {
        return order >= other.order;
    }

    public boolean lessThanOrEqualTo( MinecraftVersions other ) {
        return order <= other.order;
    }

    public static MinecraftVersions get(String v ) {
        for ( MinecraftVersions k : MinecraftVersions.values() ) {
            if ( v.contains( k.key ) ) {
                return k;
            }
        }
        return null;
    }
}
