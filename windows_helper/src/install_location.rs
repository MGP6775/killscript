use enum_ordinalize::Ordinalize;
use log::info;
use safer_ffi::ඞ::repr_c;
use safer_ffi::{derive_ReprC, ffi_export};
use std::fmt::Display;
use windows_registry::LOCAL_MACHINE;

#[derive(Ordinalize)]
enum GTAVersion {
    Legacy,
    Enhanced,
}

impl Display for GTAVersion {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        let version = match self {
            GTAVersion::Legacy => "Legacy",
            GTAVersion::Enhanced => "Enhanced",
        };

        write!(f, "{:?}", version)
    }
}

#[derive_ReprC]
#[repr(C)]
struct GtaInstallLocationResult {
    is_error: bool,
    content: repr_c::String,
}

#[ffi_export]
fn read_gta_location(gta_version: i8) -> GtaInstallLocationResult {
    let version = GTAVersion::from_ordinal(gta_version).unwrap();

    match _read_gta_location(version) {
        Ok(location) => GtaInstallLocationResult {
            is_error: false,
            content: location.into(),
        },
        Err(error) => GtaInstallLocationResult {
            is_error: true,
            content: error.to_string().into(),
        },
    }
}

#[ffi_export]
fn detect_version() -> i8 {
    match _detect_version() {
        None => -1,
        Some(version) => version.ordinal(),
    }
}

fn _detect_version() -> Option<GTAVersion> {
    let legacy = _read_gta_location(GTAVersion::Legacy);
    let enhanced = _read_gta_location(GTAVersion::Enhanced);

    if legacy.is_ok() && enhanced.is_err() {
        Some(GTAVersion::Legacy)
    } else if enhanced.is_ok() {
        Some(GTAVersion::Enhanced)
    } else {
        None
    }
}

fn _read_gta_location(gta_version: GTAVersion) -> windows_registry::Result<String> {
    let steam_key = LOCAL_MACHINE.open("SOFTWARE\\WOW6432Node\\Rockstar Games\\Steam");
    let is_steam = steam_key.is_ok();
    info!(
        "Reading GTA V install location, version: {}, is_steam: {}",
        gta_version, is_steam
    );

    let key = LOCAL_MACHINE.open(gta_version.registry_location(is_steam))?;

    if is_steam {
        key.get_string("InstallFolderSteam")
    } else {
        key.get_string("InstallFolder")
    }
}

impl GTAVersion {
    fn registry_location(&self, is_steam: bool) -> &str {
        match self {
            GTAVersion::Enhanced if !is_steam => {
                "SOFTWARE\\WOW6432Node\\Rockstar Games\\GTAV Enhanced"
            }
            GTAVersion::Enhanced => "SOFTWARE\\WOW6432Node\\Rockstar Games\\GTA V Enhanced",
            GTAVersion::Legacy => "SOFTWARE\\WOW6432Node\\Rockstar Games\\Grand Theft Auto V",
        }
    }
}
