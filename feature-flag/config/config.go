package config

import "github.com/spf13/viper"

type Config struct {
	Flags map[string]any
}

func Load() (*Config, error) {
	v := viper.New()
	v.SetConfigName("config")
	v.SetConfigType("yaml")
	v.AddConfigPath("data")
	v.AutomaticEnv()

	if err := v.ReadInConfig(); err != nil {
		if _, ok := err.(viper.ConfigFileNotFoundError); !ok {
			return nil, err
		}
	}

	var cfg Config
	if err := v.Unmarshal(&cfg); err != nil {
		return nil, err
	}

	fv := viper.New()
	fv.SetConfigFile("data/flags.yml")
	if err := fv.ReadInConfig(); err != nil {
		return nil, err
	}

	cfg.Flags = fv.AllSettings()
	return &cfg, nil
}
