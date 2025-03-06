use enum_ordinalize::Ordinalize;
use safer_ffi::ffi_export;
use safer_ffi::ඞ::repr_c;
use windows_registry::LOCAL_MACHINE;

#[derive(Ordinalize)]
enum GTAVersion {
    Legacy,
    Enhanced,
}

#[ffi_export]
fn read_gta_location(gta_version: i8) -> repr_c::String {
    let version = GTAVersion::from_ordinal(gta_version).unwrap();
    let location = _read_gta_location(version).unwrap();
    location.into()
}

#[ffi_export]
fn detect_version() -> i8 {
    match _detect_version() {
        None => -1,
        Some(version) => version.ordinal()
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
    let key = LOCAL_MACHINE.open(gta_version.registry_location())?;

    key.get_string("InstallFolder")
}

impl GTAVersion {
    fn registry_location(&self) -> &str {
        match self {
            GTAVersion::Legacy => "SOFTWARE\\WOW6432Node\\Rockstar Games\\Grand Theft Auto V",
            GTAVersion::Enhanced => "SOFTWARE\\WOW6432Node\\Rockstar Games\\GTAV Enhanced"
        }
    }
}
