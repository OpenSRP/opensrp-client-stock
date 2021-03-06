package org.smartregister.stock.openlmis.dto;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class TradeItemDto extends BaseDto implements Parcelable {

    private String name;

    private Integer totalStock;

    private Long lastUpdated;

    private Integer numberOfLots;

    private String dispensingUnit;

    private Long netContent;

    private String programId;

    private Boolean hasLots;

    private Boolean useVVM;

    private String routeOfAdministration;

    public TradeItemDto(@NonNull String id, @NonNull String name, @NonNull Integer totalStock,
                        @NonNull Long lastUpdated, Integer numberOfLots, @NonNull String dispensingUnit,
                        @NonNull Long netContent, String programId, boolean hasLots, boolean useVVM,
                        String routeOfAdministration) {
        this.id = id;
        this.name = name;
        this.totalStock = totalStock;
        this.lastUpdated = lastUpdated;
        this.numberOfLots = numberOfLots == null ? 0 : numberOfLots;
        this.dispensingUnit = dispensingUnit;
        this.netContent = netContent;
        this.programId = programId;
        this.hasLots = hasLots;
        this.useVVM = useVVM;
        this.routeOfAdministration = routeOfAdministration;
    }

    public TradeItemDto(Parcel in) {
        String[] data = new String[11];

        in.readStringArray(data);
        id = data[0];
        name = data[1];
        totalStock = Integer.valueOf(data[2]);
        lastUpdated = Long.valueOf(data[3]);
        numberOfLots = Integer.valueOf(data[4]);
        dispensingUnit = data[5];
        netContent = Long.valueOf(data[6]);
        programId = data[7];
        hasLots = Boolean.valueOf(data[8]);
        useVVM = Boolean.valueOf(data[9]);
        routeOfAdministration = data[10];
    }

    public TradeItemDto(@NonNull String id, @NonNull String name, @NonNull Integer totalStock,
                        @NonNull Long lastUpdated, Integer numberOfLots, @NonNull String dispensingUnit,
                        @NonNull Long netContent, @NonNull String routeOfAdministration) {
        this(id, name, totalStock, lastUpdated, numberOfLots, dispensingUnit, netContent, null,
                true, true, routeOfAdministration);

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{id, name,
                totalStock.toString(), lastUpdated.toString(), numberOfLots.toString(),
                dispensingUnit, netContent.toString(), programId, hasLots.toString(),
                useVVM.toString(), routeOfAdministration});
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public TradeItemDto createFromParcel(Parcel in) {
            return new TradeItemDto(in);
        }

        public TradeItemDto[] newArray(int size) {
            return new TradeItemDto[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setTotalStock(Integer totalStock) {
        this.totalStock = totalStock;
    }

    public Integer getTotalStock() {
        return totalStock;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public Integer getNumberOfLots() {
        return numberOfLots;
    }

    public String getDispensingUnit() {
        return dispensingUnit;
    }

    public Long getNetContent() {
        return netContent;
    }

    public String getProgramId() {
        return programId;
    }

    public boolean isHasLots() {
        return hasLots;
    }

    public Boolean isUseVVM() {
        return useVVM;
    }

    public String getRouteOfAdministration() {
        return routeOfAdministration;
    }
}
