package config

import "github.com/spf13/viper"

type Config struct {
	Flags map[string]any
}

func Load() (*Config, error) {
	v := viper.New()
	v.SetConfigFile("data/flags.yml")
	if err := v.ReadInConfig(); err != nil {
		return nil, err
	}

	return &Config{Flags: v.AllSettings()}, nil
}
