# Methods added to this helper will be available to all templates in the application.
module ApplicationHelper
  def grav_url_for(email, size=80)
    digest = Digest::MD5.hexdigest(email)
    return "http://www.gravatar.com/avatar/" + digest + "?s=" + size.to_s
  end
  
  def font_size_for_weight(weight)
    return (11 + weight*5).to_s + "pt"
  end
  
  def group_into_ns(functions)
    functions.sort{|a,b| a.name <=> b.name}.group_by do |f|
      name = f[:name]
      first_char = name[0,1].downcase
      if first_char == "*"
        "*"
      elsif first_char[0] < 97
        "+"
      else
        first_char
      end
    end
  end
  
  def functions_group_into_alpha(functions)
    functions.sort{|a,b| a.name <=> b.name}.group_by do |f|
      name = f.name
      first_char = name[0,1].downcase
      if first_char == "*"
        "*"
      elsif first_char[0] < 97
        "+"
      else
        first_char
      end
    end.sort{|a,b| a[0] <=> b[0]}
  end
  
  def time_ago_or_time_stamp(from_time, to_time = Time.now, include_seconds = true, detail = false)
    from_time = from_time.to_time if from_time.respond_to?(:to_time)
    to_time = to_time.to_time if to_time.respond_to?(:to_time)
    distance_in_minutes = (((to_time - from_time).abs)/60).round
    distance_in_seconds = ((to_time - from_time).abs).round
    case distance_in_minutes
      when 0..1           then time = "about " + ((distance_in_seconds < 60) ? "#{distance_in_seconds} seconds ago" : '1 minute ago')
      when 2..59          then time = "about #{distance_in_minutes} minutes ago"
      when 60..90         then time = "about 1 hour ago"
      when 90..1440       then time = "about #{(distance_in_minutes.to_f / 60.0).round} hours ago"
      when 1440..2160     then time = 'about 1 day ago' # 1-1.5 days
      when 2160..2880     then time = "about #{(distance_in_minutes.to_f / 1440.0).round} days ago" # 1.5-2 days
      else time = " on " + from_time.strftime("%a, %d %b %Y")
    end
    return time_stamp(from_time) if (detail && distance_in_minutes > 2880)
    return time
  end
  
end
