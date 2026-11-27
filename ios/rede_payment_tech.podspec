#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint rede_payment_tech.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'rede_payment_tech'
  s.version          = '2.0.0'
  s.summary          = 'Rede Smart SDK plugin (Android only; iOS is a stub).'
  s.description      = <<-DESC
Flutter plugin for Rede smart POS terminals. Only Android is supported; iOS is a stub.
                       DESC
  s.homepage         = 'https://github.com/jhonathanqz/rede_smart_flutter'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'JY Labtech' => 'https://jylabtech.com.br' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.dependency 'Flutter'
  s.platform = :ios, '12.0'

  # Flutter.framework does not contain a i386 slice.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386' }
  s.swift_version = '5.0'

  # If your plugin requires a privacy manifest, for example if it uses any
  # required reason APIs, update the PrivacyInfo.xcprivacy file to describe your
  # plugin's privacy impact, and then uncomment this line. For more information,
  # see https://developer.apple.com/documentation/bundleresources/privacy_manifest_files
  # s.resource_bundles = {'rede_payment_tech_privacy' => ['Resources/PrivacyInfo.xcprivacy']}
end
